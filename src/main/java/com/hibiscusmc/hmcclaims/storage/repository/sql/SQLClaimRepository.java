package com.hibiscusmc.hmcclaims.storage.repository.sql;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.claim.ClaimSerializer;
import com.hibiscusmc.hmcclaims.claim.RawClaim;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.impl.HikariStorage;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.util.ByteUtil;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * A SQL-based implementation of the {@link ClaimRepository} using JDBC.
 */
public class SQLClaimRepository implements ClaimRepository {

    private final HikariStorage storage;
    private final ExecutorService executor;

    private final String getAllClaimsQuery;
    private final String getAllSubClaimsQuery;
    private final String getClaimQuery;

    private final String saveClaimQuery;
    private final String saveClaimNameQuery;
    private final String saveClaimRegionQuery;
    private final String saveClaimMetaQuery;

    private final String saveMemberQuery;
    private final String saveRoleQuery;
    private final String saveSettingQuery;

    private final String deleteClaimQuery;

    public SQLClaimRepository(Settings.Storage settings, HikariStorage storage, ExecutorService executor) {
        this.storage = storage;
        this.executor = executor;

        String prefix = settings.prefix();

        this.getAllClaimsQuery = "SELECT * FROM " + prefix + "claims " +
                "WHERE world_name = ? AND parent_uuid IS NULL;";
        this.getAllSubClaimsQuery = "SELECT * FROM " + prefix + "claims " +
                "WHERE parent_uuid IS NOT NULL;";
        this.getClaimQuery = "SELECT * FROM " + prefix + "claims " +
                "WHERE uuid = ?;";

        this.saveClaimQuery = "INSERT INTO " + prefix + "claims (uuid, owner, name, world_name, min_x, max_x, min_z, max_z, parent_uuid, locked, roles, members, settings, schema_version, claimed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "owner = VALUES(owner), name = VALUES(name), world_name = VALUES(world_name), " +
                "min_x = VALUES(min_x), max_x = VALUES(max_x), min_z = VALUES(min_z), max_z = VALUES(max_z), " +
                "roles = VALUES(roles), members = VALUES(members), settings = VALUES(settings), schema_version = VALUES(schema_version), " +
                "parent_uuid = VALUES(parent_uuid), locked = VALUES(locked);";

        this.saveClaimNameQuery = "UPDATE " + prefix + "claims SET name = ? WHERE uuid = ?";
        this.saveClaimRegionQuery = "UPDATE " + prefix + "claims SET min_x = ?, max_x = ?, min_z = ?, max_z = ? WHERE uuid = ?";
        this.saveClaimMetaQuery = "UPDATE " + prefix + "claims SET name = ?, " +
                "min_x = ?, max_x = ?, min_z = ?, max_z = ?, " +
                "locked = ? WHERE uuid = ?";

        this.saveMemberQuery = "UPDATE " + prefix + "claims SET members = ? WHERE uuid = ?;";
        this.saveRoleQuery = "UPDATE " + prefix + "claims SET roles = ? WHERE uuid = ?;";
        this.saveSettingQuery = "UPDATE " + prefix + "claims SET settings = ? WHERE uuid = ?;";

        this.deleteClaimQuery = "DELETE FROM " + prefix + "claims WHERE uuid = ?;";
    }

    @Override
    public @NotNull CompletableFuture<RawClaim.CacheHolder> getAllClaims(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            Long2ObjectMap<Set<RawClaim>> chunks = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
            Map<UUID, Set<RawClaim>> players = new ConcurrentHashMap<>();

            try (Connection con = storage.getConnection()) {
                Map<UUID, Set<RawClaim>> subClaims = new HashMap<>();

                try (PreparedStatement ps = con.prepareStatement(this.getAllSubClaimsQuery)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID parentId = ByteUtil.bytesToUUID(rs.getBytes("parent_uuid"));
                            Set<RawClaim> claims = subClaims.computeIfAbsent(parentId, k -> new HashSet<>());

                            claims.add(buildRawClaim(rs, new HashSet<>()));
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(this.getAllClaimsQuery)) {
                    ps.setString(1, worldName);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            UUID claimId = ByteUtil.bytesToUUID(rs.getBytes("uuid"));

                            int minX = rs.getInt("min_x");
                            int maxX = rs.getInt("max_x");
                            int minZ = rs.getInt("min_z");
                            int maxZ = rs.getInt("max_z");

                            int minChunkX = minX >> 4;
                            int maxChunkX = maxX >> 4;
                            int minChunkZ = minZ >> 4;
                            int maxChunkZ = maxZ >> 4;

                            RawClaim claim = buildRawClaim(rs, subClaims.getOrDefault(claimId, new HashSet<>()));

                            players.computeIfAbsent(claim.owner(), k -> ConcurrentHashMap.newKeySet())
                                    .add(claim);

                            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                                    long chunkKey = ChunkUtil.getChunkKey(cx, cz);

                                    chunks.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet())
                                            .add(claim);
                                }
                            }
                        }
                    }
                }

                return new RawClaim.CacheHolder(chunks, players);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load all claims", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<RawClaim>> getClaims(@NotNull List<UUID> claimIds) {
        return CompletableFuture.supplyAsync(() -> {
            String placeholders = String.join(",", Collections.nCopies(claimIds.size(), "?"));

            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getClaimQuery
                         .replace(":id_comparison", "IN (" + placeholders + ")"))) {
                for (int i = 0; i < claimIds.size(); i++) {
                    ps.setBytes(i + 1, ByteUtil.UUIDtoBytes(claimIds.get(i)));
                }

                List<RawClaim> claims = new ArrayList<>();

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        claims.add(buildRawClaim(rs, new HashSet<>()));
                    }
                }

                return claims;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load claim " + claimIds, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<RawClaim> getClaim(@NotNull UUID claimId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getClaimQuery
                         .replace(":id_comparison", "= ?"))) {
                ps.setBytes(1, ByteUtil.UUIDtoBytes(claimId));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return buildRawClaim(rs, new HashSet<>());
                    }

                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaim(@NotNull Claim claim) {
        ClaimRegion region = claim.region();

        return saveClaim(new RawClaim(
                claim.claimId(), claim.owner(), claim.name(),
                claim.main() != null ? claim.main().claimId() : null, Set.of(),
                region.worldName(), region.minX(), region.maxX(), region.minZ(), region.maxZ(),
                ClaimSerializer.serialize(claim.roleRegistry()),
                ClaimSerializer.serialize(claim.allMembers()),
                ClaimSerializer.serialize(claim.settings()),
                claim.locked(), claim.claimedTimestamp()
        ));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaim(@NotNull RawClaim claim) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveClaimQuery)) {
                ps.setBytes(1, ByteUtil.UUIDtoBytes(claim.claimId()));
                ps.setBytes(2, ByteUtil.UUIDtoBytes(claim.owner()));
                ps.setString(3, claim.name());
                ps.setString(4, claim.worldName());
                ps.setInt(5, claim.minX());
                ps.setInt(6, claim.maxX());
                ps.setInt(7, claim.minZ());
                ps.setInt(8, claim.maxZ());

                if (claim.parentId() != null) {
                    ps.setBytes(9, ByteUtil.UUIDtoBytes(claim.parentId()));
                } else {
                    ps.setNull(9, Types.BINARY);
                }

                ps.setBoolean(10, claim.locked());

                ps.setBytes(11, claim.rolesBytes());
                ps.setBytes(12, claim.membersBytes());
                ps.setBytes(13, claim.settingsBytes());

                ps.setInt(14, SCHEMA_VERSION);

                ps.setTimestamp(15, Timestamp.from(claim.claimedTimestamp()));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save claim " + claim.claimId(), e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaimName(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        String name = claim.name();

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveClaimNameQuery)) {
                ps.setString(1, name);
                ps.setBytes(2, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save name for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaimRegion(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        ClaimRegion region = claim.region();

        int minX = region.minX();
        int maxX = region.maxX();
        int minZ = region.minZ();
        int maxZ = region.maxZ();

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveClaimRegionQuery)) {
                ps.setInt(1, minX);
                ps.setInt(2, maxX);
                ps.setInt(3, minZ);
                ps.setInt(4, maxZ);
                ps.setBytes(5, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save region for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaimMeta(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        String name = claim.name();
        boolean locked = claim.locked();
        ClaimRegion region = claim.region();

        int minX = region.minX();
        int maxX = region.maxX();
        int minZ = region.minZ();
        int maxZ = region.maxZ();

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveClaimMetaQuery)) {
                ps.setString(1, name);
                ps.setInt(2, minX);
                ps.setInt(3, maxX);
                ps.setInt(4, minZ);
                ps.setInt(5, maxZ);
                ps.setBoolean(6, locked);
                ps.setBytes(7, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save meta for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveMembers(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        byte[] members = ClaimSerializer.serialize(claim.allMembers());

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveMemberQuery)) {
                ps.setBytes(1, members);
                ps.setBytes(2, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save members for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveRoles(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        byte[] roles = ClaimSerializer.serialize(claim.roleRegistry());

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveRoleQuery)) {
                ps.setBytes(1, roles);
                ps.setBytes(2, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save roles for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveSettings(@NotNull Claim claim) {
        UUID claimId = claim.claimId();
        byte[] settings = ClaimSerializer.serialize(claim.settings());

        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveSettingQuery)) {
                ps.setBytes(1, settings);
                ps.setBytes(2, ByteUtil.UUIDtoBytes(claimId));

                ps.addBatch();

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save settings for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteClaim(@NotNull UUID claimId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.deleteClaimQuery)) {
                ps.setBytes(1, ByteUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete claim " + claimId, e);
            }
        }, executor);
    }

    /**
     * Maps the current row of a {@link ResultSet} into a lightweight {@link RawClaim} instance.
     *
     * @param rs        The active database {@link ResultSet} positioned at the target row.
     * @param subClaims A list of every sub-claim that its owned by this claim.
     * @return A {@link RawClaim} containing the data snapshot.
     * @throws SQLException If a database access error occurs, or if a column name is missing
     *                      or misaligned within the query statement.
     */
    private RawClaim buildRawClaim(ResultSet rs, Set<RawClaim> subClaims) throws SQLException {
        byte[] parentId = rs.getBytes("parent_uuid");
        return new RawClaim(
                ByteUtil.bytesToUUID(rs.getBytes("uuid")),
                ByteUtil.bytesToUUID(rs.getBytes("owner")),
                rs.getString("name"),
                parentId != null ? ByteUtil.bytesToUUID(parentId) : null,
                subClaims,
                rs.getString("world_name"),
                rs.getInt("min_x"),
                rs.getInt("max_x"),
                rs.getInt("min_z"),
                rs.getInt("max_z"),
                rs.getBytes("roles"),
                rs.getBytes("members"),
                rs.getBytes("settings"),
                rs.getBoolean("locked"),
                rs.getTimestamp("claimed_at").toInstant()
        );
    }
}