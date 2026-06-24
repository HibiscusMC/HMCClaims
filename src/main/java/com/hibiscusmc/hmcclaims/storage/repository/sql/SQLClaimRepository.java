package com.hibiscusmc.hmcclaims.storage.repository.sql;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.impl.remote.HikariStorage;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.util.SQLUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * A SQL-based implementation of the {@link ClaimRepository} using JDBC.
 */
public class SQLClaimRepository implements ClaimRepository {

    private final HikariStorage storage;
    private final ExecutorService executor;

    private final String getAllClaimChunksQuery;
    private final String getAllClaimsQuery;
    private final String getAllSubClaimsQuery;
    private final String getClaimQuery;
    private final String getMembersQuery;
    private final String getMemberPermissionsQuery;

    private final String getRolesQuery;
    private final String getRolePermissionsQuery;

    private final String saveClaimQuery;
    private final String saveClaimChunkQuery;
    private final String saveMemberQuery;
    private final String saveSettingQuery;
    private final String savePermissionQuery;
    private final String saveRoleQuery;

    private final String deleteClaimQuery;
    private final String deleteMemberQuery;

    public SQLClaimRepository(Settings.Storage settings, HikariStorage storage, ExecutorService executor) {
        this.storage = storage;
        this.executor = executor;

        String prefix = settings.prefix();

        this.getAllClaimChunksQuery = "SELECT * FROM " + prefix + "claim_chunks WHERE chunk_key = ?;";
        this.getAllClaimsQuery = "SELECT c.*, u.last_known_name AS owner_last_known_name FROM " + prefix + "claims c " +
                "INNER JOIN " + prefix + "users u " +
                "ON c.owner = u.uuid " +
                "WHERE c.uuid IN (:uuids) AND parent_uuid IS NULL;";
        this.getAllSubClaimsQuery = "SELECT * FROM " + prefix + "claims WHERE parent_uuid = ?;";
        this.getClaimQuery = "SELECT c.*, u.last_known_name AS owner_last_known_name FROM " + prefix + "claims c " +
                "INNER JOIN " + prefix + "users u " +
                "ON c.owner = u.uuid " +
                "WHERE c.uuid = ? AND parent_uuid IS NULL;";
        this.getMembersQuery = "SELECT m.*, u.last_known_name FROM " + prefix + "members m " +
                "INNER JOIN " + prefix + "users u " +
                "ON m.player_uuid = u.uuid " +
                "WHERE m.claim_uuid = ?;";
        this.getMemberPermissionsQuery = "SELECT * FROM " + prefix + "permissions " +
                "WHERE claim_uuid = ? AND player_uuid = ?;";

        this.getRolesQuery = "SELECT * FROM " + prefix + "claim_roles WHERE claim_uuid = ?;";
        this.getRolePermissionsQuery = "SELECT * FROM " + prefix + "role_permissions WHERE claim_uuid = ? AND role_id = ?;";

        this.saveClaimQuery = "INSERT INTO " + prefix + "claims (uuid, owner, name, world_name, region, parent_uuid, locked, claimed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "owner = VALUES(owner), name = VALUES(name), world_name = VALUES(world_name), " +
                "region = VALUES(region), parent_uuid = VALUES(parent_uuid), locked = VALUES(locked);";
        this.saveClaimChunkQuery = "INSERT INTO " + prefix + "claim_chunks (claim_uuid, chunk_key) " +
                "VALUES (?, ?) ON DUPLICATE KEY UPDATE " +
                "claim_uuid = VALUES(claim_uuid), chunk_key = VALUES(chunk_key);";
        this.saveMemberQuery = "INSERT INTO " + prefix + "members (claim_uuid, player_uuid, role, banned, joined_at) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "role = VALUES(role), banned = VALUES(banned);";
        this.saveSettingQuery = "INSERT INTO " + prefix + "claim_settings (claim_uuid, setting_key, setting_value) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "setting_value = VALUES(setting_value);";
        this.savePermissionQuery = "INSERT INTO " + prefix + "permissions (claim_uuid, player_uuid, permission, value) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "value = VALUES(value);";
        this.saveRoleQuery = "INSERT INTO " + prefix + "claim_roles (claim_uuid, role_id, name, position) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), position = VALUES(position);";

        this.deleteClaimQuery = "DELETE FROM " + prefix + "claims WHERE uuid = ?;";
        this.deleteMemberQuery = "DELETE FROM " + prefix + "members WHERE claim_uuid = ? AND player_uuid = ?;";
    }

    @Override
    public @NotNull CompletableFuture<List<Claim>> getAllClaims(long chunkKey) {
        return CompletableFuture.supplyAsync(() -> {
            List<Claim> claims = new ArrayList<>();
            List<byte[]> bytes = new ArrayList<>();

            try (Connection con = storage.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement(this.getAllClaimChunksQuery)) {
                    ps.setLong(1, chunkKey);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            bytes.add(rs.getBytes("claim_uuid"));
                        }
                    }
                }

                if (bytes.isEmpty()) {
                    return claims;
                }

                String placeholders = String.join(",", Collections.nCopies(bytes.size(), "?"));
                try (PreparedStatement ps2 = con.prepareStatement(
                        this.getAllClaimsQuery
                                .replace(":uuids", placeholders))
                ) {
                    for (int i = 0; i < bytes.size(); i++) {
                        ps2.setBytes(i + 1, bytes.get(i));
                    }

                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            claims.add(buildClaim(rs2, null));
                        }
                    }
                }

                return claims;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load all claims", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<Claim>> getAllSubClaims(@NotNull Claim mainClaim) {
        return CompletableFuture.supplyAsync(() -> {
            List<Claim> claims = new ArrayList<>();

            try (Connection con = storage.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement(this.getAllSubClaimsQuery)) {
                    ps.setBytes(1, SQLUtil.UUIDtoBytes(mainClaim.claimId()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            claims.add(buildClaim(rs, mainClaim));
                        }
                    }
                }

                return claims;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load all claims", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Claim> getClaim(@NotNull UUID claimId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getClaimQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return buildClaim(rs, null);
                    }
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<ClaimMember>> getClaimMembers(@NotNull Claim claim) {
        return CompletableFuture.supplyAsync(() -> {
            List<ClaimMember> members = new ArrayList<>();

            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getMembersQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claim.claimId()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        members.add(buildMember(claim, rs));
                    }
                }

                return members;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load members for claim " + claim.claimId(), e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Set<PermissionHolder>> getClaimMemberPermissions(@NotNull UUID claimId, @NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            Set<PermissionHolder> permissions = new HashSet<>();

            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getMemberPermissionsQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));
                ps.setBytes(2, SQLUtil.UUIDtoBytes(playerId));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String permission = rs.getString("permission");
                        boolean value = rs.getBoolean("value");

                        permissions.add(new PermissionHolder(PermissionRegistry.getPermission(permission), value));
                    }
                }

                return permissions;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load permissions for member " + playerId + " in claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaim(@NotNull Claim claim) {
        CompletableFuture<Void> claimFuture = CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveClaimQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claim.claimId()));
                ps.setBytes(2, SQLUtil.UUIDtoBytes(claim.owner().uuid()));
                ps.setString(3, claim.name());
                ps.setString(4, claim.region().worldName());
                ps.setString(5, claim.region().toString());

                if (claim.main() != null) {
                    ps.setBytes(6, SQLUtil.UUIDtoBytes(claim.main().claimId()));
                } else {
                    ps.setNull(6, Types.BINARY);
                }

                ps.setBoolean(7, claim.locked());
                ps.setTimestamp(8, Timestamp.from(claim.claimedTimestamp()));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save claim " + claim.claimId(), e);
            }
        }, executor);

        return CompletableFuture.allOf(
                claimFuture,
                saveClaimChunks(claim),
                saveMembers(claim.claimId(), claim.members()),
                saveSettings(claim.claimId(), claim.settings()),
                saveRoles(claim.claimId(), claim.roleRegistry().allRoles())
        );
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaimChunks(@NotNull Claim claim) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.saveClaimChunkQuery)) {
                    byte[] claimIdBytes = SQLUtil.UUIDtoBytes(claim.claimId());
                    LongSet chunks = claim.chunks();

                    for (Long chunk : chunks) {
                        ps.setBytes(1, claimIdBytes);
                        ps.setLong(2, chunk);

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save chunks for claim " + claim.claimId(), e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveMembers(@NotNull UUID claimId, @NotNull Collection<ClaimMember> members) {
        return CompletableFuture.runAsync(() -> {
            if (members.isEmpty()) return;

            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.saveMemberQuery)) {
                    byte[] claimIdBytes = SQLUtil.UUIDtoBytes(claimId);

                    for (ClaimMember member : members) {
                        ps.setBytes(1, claimIdBytes);
                        ps.setBytes(2, SQLUtil.UUIDtoBytes(member.uuid()));
                        ps.setString(3, member.role().id());
                        ps.setBoolean(4, member.banned());
                        ps.setTimestamp(5, Timestamp.from(member.joinedTimestamp()));

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save members for claim " + claimId, e);
            }

            for (ClaimMember member : members) {
                savePermissions(claimId, member.uuid(), member.permissions());
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveMember(@NotNull UUID claimId, @NotNull ClaimMember member) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveMemberQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));
                ps.setBytes(2, SQLUtil.UUIDtoBytes(member.uuid()));
                ps.setString(3, member.role().id());
                ps.setBoolean(4, member.banned());
                ps.setTimestamp(5, Timestamp.from(member.joinedTimestamp()));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save member " + member.uuid() + " for claim " + claimId, e);
            }

            savePermissions(claimId, member.uuid(), member.permissions());
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveSettings(@NotNull UUID claimId, @NotNull Map<String, SettingHolder<?>> settings) {
        return CompletableFuture.runAsync(() -> {
            if (settings.isEmpty()) return;

            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.saveSettingQuery)) {
                    byte[] claimIdBytes = SQLUtil.UUIDtoBytes(claimId);

                    for (Map.Entry<String, SettingHolder<?>> entry : settings.entrySet()) {
                        ps.setBytes(1, claimIdBytes);
                        ps.setString(2, entry.getKey());
                        ps.setString(3, entry.getValue().value().toString());

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save settings for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> savePermissions(@NotNull UUID claimId, @NotNull UUID playerId, @NotNull Set<PermissionHolder> permissions) {
        return CompletableFuture.runAsync(() -> {
            if (permissions.isEmpty()) return;

            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.savePermissionQuery)) {
                    byte[] claimIdBytes = SQLUtil.UUIDtoBytes(claimId);
                    byte[] playerIdBytes = SQLUtil.UUIDtoBytes(playerId);

                    for (PermissionHolder holder : permissions) {
                        ps.setBytes(1, claimIdBytes);
                        ps.setBytes(2, playerIdBytes);
                        ps.setString(3, holder.permission().key().asString());
                        ps.setBoolean(4, holder.status());

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save permissions for player " + playerId + " in claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveRoles(@NotNull UUID claimId, @NotNull List<ClaimRole> roles) {
        return CompletableFuture.runAsync(() -> {
            if (roles.isEmpty()) return;

            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.saveRoleQuery)) {
                    byte[] claimIdBytes = SQLUtil.UUIDtoBytes(claimId);

                    for (ClaimRole role : roles) {
                        ps.setBytes(1, claimIdBytes);
                        ps.setString(2, role.id());
                        ps.setString(3, role.name());
                        ps.setInt(4, roles.indexOf(role));

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save roles for claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteClaim(@NotNull UUID claimId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.deleteClaimQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete claim " + claimId, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteMember(@NotNull UUID claimId, @NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.deleteMemberQuery)) {
                ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));
                ps.setBytes(2, SQLUtil.UUIDtoBytes(playerId));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete claim " + claimId, e);
            }
        }, executor);
    }

    @NotNull
    private Set<Permission> getClaimRolePermissions(UUID claimId, String roleId) {
        Set<Permission> permissions = new HashSet<>();

        try (Connection con = storage.getConnection();
             PreparedStatement ps = con.prepareStatement(this.getRolePermissionsQuery)) {
            ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));
            ps.setString(2, roleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(PermissionRegistry.getPermission(rs.getString("permission")));
                }

                return permissions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load permissions for role " + roleId + " in claim " + claimId, e);
        }
    }

    private @NotNull List<ClaimRole> getClaimRoles(UUID claimId) {
        List<ClaimRole> roles = new ArrayList<>();

        try (Connection con = storage.getConnection();
             PreparedStatement ps = con.prepareStatement(this.getRolesQuery)) {
            ps.setBytes(1, SQLUtil.UUIDtoBytes(claimId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roleId = rs.getString("role_id");
                    Set<Permission> permissions = getClaimRolePermissions(claimId, roleId);

                    ClaimRole role = new ClaimRole(
                            roleId,
                            rs.getString("name"),
                            permissions
                    );
                    role.position(rs.getInt("position"));

                    roles.add(role);
                }

                roles.sort(Comparator.comparingInt(ClaimRole::position));
                return roles;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load roles for claim " + claimId, e);
        }
    }

    /**
     * Maps a single row from a {@link ResultSet} into a {@link ClaimMember} object.
     *
     * @param claim The claim this member belongs to.
     * @param rs    The result set positioned at the desired row.
     * @return A fully populated base ClaimMember instance.
     * @throws SQLException If a required column is missing or data conversion fails.
     */
    private ClaimMember buildMember(Claim claim, ResultSet rs) throws SQLException {
        UUID playerId = SQLUtil.bytesToUUID(rs.getBytes("player_uuid"));
        String lastKnownName = rs.getString("last_known_name");

        String roleName = rs.getString("role");
        Optional<ClaimRole> role = claim.roleRegistry()
                .find(roleName);

        Set<PermissionHolder> permissions = getClaimMemberPermissions(claim.claimId(), playerId)
                .join();

        ClaimMember member = new ClaimMember(
                playerId, claim, lastKnownName, role.orElse(claim.roleRegistry().defaultRole()), permissions
        );
        member.banned(rs.getBoolean("banned"));
        member.joinedTimestamp(rs.getTimestamp("joined_at").toInstant());

        return member;
    }

    /**
     * Maps a single row from a {@link ResultSet} into a {@link Claim} object.
     *
     * @param rs        The result set positioned at the desired row.
     * @param mainClaim The claim this sub-claim belongs to.
     * @return A fully populated base Claim instance.
     * @throws SQLException If a required column is missing or data conversion fails.
     */
    private Claim buildClaim(ResultSet rs, Claim mainClaim) throws SQLException {
        UUID uuid = SQLUtil.bytesToUUID(rs.getBytes("uuid"));
        String name = rs.getString("name");

        String[] serializedRegion = rs.getString("region")
                .split(";");

        ClaimRegion region = new ClaimRegion(
                rs.getString("world_name"),
                Integer.parseInt(serializedRegion[0]),
                Integer.parseInt(serializedRegion[1]),
                Integer.parseInt(serializedRegion[2]),
                Integer.parseInt(serializedRegion[3])
        );

        NameAndId owner;
        if (mainClaim == null) {
            UUID ownerId = SQLUtil.bytesToUUID(rs.getBytes("owner"));
            String ownerName = rs.getString("owner_last_known_name");
            owner = new NameAndId(ownerId, ownerName);
        } else {
            owner = new NameAndId(
                    mainClaim.owner().uuid(),
                    mainClaim.owner().lastKnownName()
            );
        }

        List<ClaimRole> roles = getClaimRoles(uuid);

        Claim claim = new Claim(
                uuid, name, mainClaim, owner,
                region, roles, rs.getTimestamp("claimed_at").toInstant()
        );
        claim.locked(rs.getBoolean("locked"));

        List<ClaimMember> members = getClaimMembers(claim)
                .join();
        members.forEach(claim::addMember);

        if (mainClaim == null) {
            List<Claim> subClaims = getAllSubClaims(claim)
                    .join();

            subClaims.forEach(claim::addSubClaim);
        }

        return claim;
    }
}