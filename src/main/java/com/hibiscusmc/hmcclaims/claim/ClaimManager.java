package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import net.minecraft.server.players.NameAndId;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The central authority for managing claim lifecycles and spatial lookups.
 */
@Singleton
public class ClaimManager {

    /**
     * Spatial index: World Name -> Chunk Key (Long) -> List of Claims in that chunk.
     */
    @Getter
    private final Map<String, Long2ObjectMap<Set<Claim>>> worldChunkMaps
            = new ConcurrentHashMap<>();

    /**
     * Maps claims inside a world
     */
    @Getter
    private final Map<String, Set<Claim>> worldClaims
            = new ConcurrentHashMap<>();

    /**
     * Maps Player UUIDs to their owned claims for quick profile lookups.
     */
    private final Map<UUID, Set<Claim>> playerClaims
            = new ConcurrentHashMap<>();

    /**
     * Maps claim UUIDs for quick claim lookups.
     */
    @Getter
    private final Map<UUID, Claim> claims
            = new ConcurrentHashMap<>();

    /**
     * Set of claims that are still loading and players shouldn't be able to
     * interact in.
     */
    private final Set<UUID> pendingClaims
            = ConcurrentHashMap.newKeySet();

    @Inject
    private ConfigHolder<DefaultRoles> rolesHolder;

    @Inject
    private UserManager userManager;

    @Inject
    private StorageHolder storageHolder;

    /**
     * Checks if the claim is still loading.
     *
     * @param claimId The id of the claim to check.
     * @return {@code true} if the claim is still loading, {@code false} otherwise.
     */
    public boolean isClaimLoading(UUID claimId) {
        return pendingClaims.contains(claimId);
    }

    /**
     * Adds a claim to the pending set.
     *
     * @param claimId The id of the claim to check.
     */
    public void addPendingClaim(UUID claimId) {
        pendingClaims.add(claimId);
    }

    /**
     * Removes a claim from the pending set.
     *
     * @param claimId The id of the claim to check.
     */
    public void removePendingClaim(UUID claimId) {
        pendingClaims.remove(claimId);
    }

    /**
     * Creates a new claim, registers it in the cache, and updates user claim blocks.
     *
     * @param player The owner of the claim.
     * @param region The physical bounds.
     * @param main   The main claim, if creating a sub-claim.
     * @return The newly created {@link Claim} instance.
     */
    @Contract("_, _, _ -> new")
    public Claim createClaim(@NotNull Player player, @NotNull ClaimRegion region, @Nullable Claim main) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User is not loaded"));

        long totalMainClaims = playerClaims.getOrDefault(player.getUniqueId(), new HashSet<>())
                .stream()
                .filter(claim -> claim.main() == null)
                .count();

        Claim newClaim = new Claim(
                UUID.randomUUID(),
                main,
                new NameAndId(player.getUniqueId(), player.getName()),
                region,
                new ArrayList<>(rolesHolder.get().defaultRoles()),
                (int) totalMainClaims + 1
        );

        if (main != null) {
            main.addSubClaim(newClaim);
        }

        addClaimToCache(newClaim);

        Storage storage = storageHolder.get();

        ClaimRepository claimRepository = storage.claims();
        if (!user.persistent()) {
            storage.users().saveUser(user)
                    .thenAccept(v -> {
                        user.persistent(true);
                        claimRepository.saveClaim(newClaim);
                    });
        } else {
            claimRepository.saveClaim(newClaim);
        }

        return newClaim;
    }

    /**
     * Indexes a claim into both the player-lookup and spatial-lookup caches.
     *
     * @param claim The claim to cache.
     */
    public void addClaimToCache(@NotNull Claim claim) {
        ClaimRegion region = claim.region();
        String world = region.worldName();

        playerClaims.computeIfAbsent(claim.owner(), k -> ConcurrentHashMap.newKeySet()).add(claim);
        worldClaims.computeIfAbsent(world, k -> ConcurrentHashMap.newKeySet()).add(claim);
        claims.put(claim.claimId(), claim);

        Long2ObjectMap<Set<Claim>> chunkMap = worldChunkMaps.computeIfAbsent(world, k ->
                Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
        );

        int minX = region.minX() >> 4;
        int maxX = region.maxX() >> 4;
        int minZ = region.minZ() >> 4;
        int maxZ = region.maxZ() >> 4;

        LongSet chunks = new LongArraySet();

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long key = ChunkUtil.getChunkKey(cx, cz);

                chunks.add(key);
                chunkMap.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                        .add(claim);
            }
        }

        claim.chunks(chunks);
    }

    /**
     * Removes a claim from both the player-lookup and spatial-lookup caches.
     *
     * @param claim The claim to remove from cache.
     */
    public void removeClaimFromCache(@NotNull Claim claim) {
        ClaimRegion region = claim.region();
        String world = region.worldName();

        Set<Claim> userClaims = playerClaims.get(claim.owner());
        if (userClaims != null) {
            userClaims.remove(claim);

            if (userClaims.isEmpty()) {
                playerClaims.remove(claim.owner());
            }
        }

        Set<Claim> wordClaimSet = worldClaims.get(world);
        if (wordClaimSet != null) {
            wordClaimSet.remove(claim);

            if (wordClaimSet.isEmpty()) {
                worldClaims.remove(world);
            }
        }

        claims.remove(claim.claimId());

        Long2ObjectMap<Set<Claim>> chunkMap = worldChunkMaps.get(world);

        if (chunkMap != null) {
            int minX = region.minX() >> 4;
            int maxX = region.maxX() >> 4;
            int minZ = region.minZ() >> 4;
            int maxZ = region.maxZ() >> 4;

            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    long key = ChunkUtil.getChunkKey(cx, cz);

                    Set<Claim> claimsInChunk = chunkMap.get(key);
                    if (claimsInChunk != null) {
                        claimsInChunk.remove(claim);

                        if (claimsInChunk.isEmpty()) {
                            chunkMap.remove(key);
                        }
                    }
                }
            }

            if (chunkMap.isEmpty()) {
                worldChunkMaps.remove(world);
            }
        }

        claim.chunks(new LongArraySet());
    }

    /**
     * Finds all claims covering a specific location.
     *
     * @param loc The location to check.
     * @return A list of claims found at this point; empty list if none.
     */
    @NotNull
    @Contract(pure = true)
    public List<Claim> getClaimsAt(@NotNull Location loc) {
        Map<Long, Set<Claim>> chunkMap = worldChunkMaps.get(loc.getWorld().getName());
        if (chunkMap == null) {
            return List.of();
        }

        long key = ChunkUtil.getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        Set<Claim> claimsInChunk = chunkMap.get(key);

        if (claimsInChunk == null || claimsInChunk.isEmpty()) {
            return List.of();
        }

        return claimsInChunk.stream()
                .filter(claim -> claim.region().contains(loc))
                .toList();
    }

    /**
     * Gets the "most specific" claim at a location.
     * If multiple claims exist (e.g., a sub-claim inside a main claim),
     * the one with the highest depth is returned.
     *
     * @param loc The location to check.
     * @return An Optional containing the deepest claim at the point.
     */
    @NotNull
    @Contract(pure = true)
    public Optional<Claim> getClaimAt(@NotNull Location loc) {
        List<Claim> claims = getClaimsAt(loc);
        if (claims.isEmpty()) {
            return Optional.empty();
        }

        return claims.stream()
                .max(Comparator.comparingInt(this::getClaimDepth));
    }

    /**
     * Gets a claim by its UUID.
     *
     * @param uuid The claim UUID.
     * @return An optional containing the claim with that UUID.
     */
    @NotNull
    @Contract(pure = true)
    public Optional<Claim> getClaim(@NotNull UUID uuid) {
        return Optional.ofNullable(claims.get(uuid));
    }

    /**
     * Obtains the claim depth.
     *
     * @param claim the claim to check
     * @return {@code 0} if it's a main claim, {@code 1} if it's a sub-claim.
     */
    @Contract(pure = true)
    private int getClaimDepth(Claim claim) {
        return (claim.main() == null) ? 0 : 1;
    }

    /**
     * Deletes a claim and removes it from cache
     *
     * @param claim the claim to delete
     */
    public void deleteClaim(@NotNull Claim claim) {
        removeClaimFromCache(claim);

        Storage storage = storageHolder.get();

        ClaimRepository claimRepository = storage.claims();
        claimRepository.deleteClaim(claim.claimId());

        if (!claim.subClaims().isEmpty()) {
            claim.subClaims().forEach(this::deleteClaim);
        }

        if (claim.main() != null) {
            claim.main().removeSubClaim(claim);
        }
    }

    /**
     * Gets all the player claims
     *
     * @param uuid the uuid of the owner of the claims
     * @return A {@link List} of {@link Claim}s that are owned by the player
     */
    @NotNull
    @Contract(pure = true)
    public Set<Claim> getPlayerClaims(@NotNull UUID uuid) {
        return playerClaims.getOrDefault(uuid, Collections.emptySet());
    }

    /**
     * Transfers a player claim and all of its sub claims to a new player
     */
    public void transferClaim(@NotNull Claim claimToTransfer, @NotNull UUID oldId, @NotNull UUID newId) {
        Set<Claim> claims = playerClaims.get(oldId);
        if (claims == null || claims.isEmpty()) {
            return;
        }

        Set<Claim> newClaims = playerClaims.computeIfAbsent(newId, k -> ConcurrentHashMap.newKeySet());

        claims.removeIf(claim -> {
            if (claim.claimId().equals(claimToTransfer.claimId()) ||
                    (claim.main() != null && claim.main().claimId().equals(claimToTransfer.claimId()))
            ) {
                newClaims.add(claim);
                return true;
            }

            return false;
        });
    }

    /**
     * Checks if a new region overlaps with any existing claims.
     *
     * @param newRegion     The region to test.
     * @param playerId      The player attempting the claim.
     * @param checkForSub   Whether to allow overlap with the player's own main claims (for sub-claiming).
     * @param resizingClaim The claim this player is resizing, {@code null} if it's a new claim.
     * @return {@code true} if an illegal overlap is detected.
     */
    @Contract(pure = true)
    public boolean isOverlapping(@NotNull ClaimRegion newRegion, @NotNull UUID playerId, boolean checkForSub, Claim resizingClaim) {
        int minX = newRegion.minX() >> 4;
        int maxX = newRegion.maxX() >> 4;
        int minZ = newRegion.minZ() >> 4;
        int maxZ = newRegion.maxZ() >> 4;

        Map<Long, Set<Claim>> worldClaims = worldChunkMaps.get(newRegion.worldName());
        if (worldClaims == null || worldClaims.isEmpty()) {
            return false;
        }

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = ChunkUtil.getChunkKey(cx, cz);
                Set<Claim> claimsInChunk = worldClaims.get(chunkKey);

                if (claimsInChunk == null) {
                    continue;
                }

                for (Claim existingClaim : claimsInChunk) {
                    if (resizingClaim != null) {
                        if (resizingClaim.claimId().equals(existingClaim.claimId())) {
                            continue;
                        }

                        if (existingClaim.main() != null && resizingClaim.claimId().equals(existingClaim.main().claimId())) {
                            continue;
                        }
                    }

                    if (checkForSub && existingClaim.owner().equals(playerId) && existingClaim.main() == null) {
                        continue;
                    }

                    if (regionsOverlap(newRegion, existingClaim.region())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if two regions are overlapping
     *
     * @param a the first region
     * @param b the second region
     * @return {@code true} if the regions overlap
     */
    private boolean regionsOverlap(@NotNull ClaimRegion a, @NotNull ClaimRegion b) {
        if (!a.worldName().equals(b.worldName())) {
            return false;
        }

        return a.minX() <= b.maxX() && a.maxX() >= b.minX() &&
                a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }
}