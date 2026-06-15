package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.user.UserManager;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The central authority for managing claim lifecycles and spatial lookups.
 */
@Singleton
public class ClaimManager {

    /**
     * Spatial index: World Name -> Chunk Key (Long) -> List of Claims in that chunk.
     */
    private final Map<String, Map<Long, List<Claim>>> worldChunkMaps
            = new ConcurrentHashMap<>();

    /**
     * Maps Player UUIDs to their owned claims for quick profile lookups.
     */
    private final Map<UUID, List<Claim>> playerClaims
            = new ConcurrentHashMap<>();

    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<DefaultRoles> rolesHolder;

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
        long totalMainClaims = playerClaims.getOrDefault(player.getUniqueId(), List.of())
                .stream()
                .filter(claim -> claim.main() == null)
                .count();

        Claim newClaim = new Claim(
                UUID.randomUUID(),
                main,
                new NameAndId(player.getUniqueId(), player.getName()),
                region,
                rolesHolder.get().defaultRoles(),
                (int) totalMainClaims + 1
        );

        if (main != null) {
            main.addSubClaim(newClaim);
        }

        addClaimToCache(newClaim);
        userManager.calculateUsedBlocks(player.getUniqueId());

        return newClaim;
    }

    /**
     * Indexes a claim into both the player-lookup and spatial-lookup caches.
     *
     * @param claim The claim to cache.
     */
    public void addClaimToCache(@NotNull Claim claim) {
        playerClaims.computeIfAbsent(claim.owner().uuid(), k -> Collections.synchronizedList(new ArrayList<>())).add(claim);

        ClaimRegion region = claim.region();
        String world = region.worldName();

        Map<Long, List<Claim>> chunkMap = worldChunkMaps.computeIfAbsent(world, k -> new ConcurrentHashMap<>());

        int minX = region.minX() >> 4;
        int maxX = region.maxX() >> 4;
        int minZ = region.minZ() >> 4;
        int maxZ = region.maxZ() >> 4;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long key = getChunkKey(cx, cz);
                chunkMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(claim);
            }
        }
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
        Map<Long, List<Claim>> chunkMap = worldChunkMaps.get(loc.getWorld().getName());
        if (chunkMap == null) {
            return List.of();
        }

        long key = getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        List<Claim> claimsInChunk = chunkMap.get(key);

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
     * Deletes a claim.
     *
     * @param claim the claim to delete
     */
    public void deleteClaim(@NotNull Claim claim) {
        List<Claim> playerClaims = this.playerClaims.get(claim.owner().uuid());
        if (playerClaims != null) {
            playerClaims.remove(claim);

            if (playerClaims.isEmpty()) {
                this.playerClaims.remove(claim.owner().uuid());
            }
        }

        Map<Long, List<Claim>> chunkMap = worldChunkMaps.get(claim.region().worldName());
        if (chunkMap == null) {
            return;
        }

        ClaimRegion region = claim.region();
        for (int cx = region.minX() >> 4; cx <= region.maxX() >> 4; cx++) {
            for (int cz = region.minZ() >> 4; cz <= region.maxZ() >> 4; cz++) {
                long key = getChunkKey(cx, cz);
                List<Claim> list = chunkMap.get(key);

                if (list != null) {
                    list.remove(claim);

                    if (list.isEmpty()) {
                        chunkMap.remove(key);
                    }
                }
            }
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
    public List<Claim> getPlayerClaims(@NotNull UUID uuid) {
        return playerClaims.getOrDefault(uuid, Collections.emptyList());
    }

    /**
     * Transfers a player claim and all of its sub claims to a new player
     */
    public void transferClaim(@NotNull Claim claimToTransfer, @NotNull UUID oldId, @NotNull UUID newId) {
        List<Claim> claims = playerClaims.get(oldId);
        if (claims == null || claims.isEmpty()) {
            return;
        }

        List<Claim> newClaims = playerClaims.computeIfAbsent(newId, k -> Collections.synchronizedList(new ArrayList<>()));

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
     * @param newRegion   The region to test.
     * @param playerId    The player attempting the claim.
     * @param checkForSub Whether to allow overlap with the player's own main claims (for sub-claiming).
     * @return {@code true} if an illegal overlap is detected.
     */
    @Contract(pure = true)
    public boolean isOverlapping(@NotNull ClaimRegion newRegion, @NotNull UUID playerId, boolean checkForSub) {
        int minX = newRegion.minX() >> 4;
        int maxX = newRegion.maxX() >> 4;
        int minZ = newRegion.minZ() >> 4;
        int maxZ = newRegion.maxZ() >> 4;

        Map<Long, List<Claim>> worldClaims = worldChunkMaps.get(newRegion.worldName());
        if (worldClaims == null || worldClaims.isEmpty()) {
            return false;
        }

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = getChunkKey(cx, cz);
                List<Claim> claimsInChunk = worldClaims.get(chunkKey);

                if (claimsInChunk == null) {
                    continue;
                }

                for (Claim existingClaim : claimsInChunk) {
                    if (checkForSub && existingClaim.owner().uuid().equals(playerId) && existingClaim.main() == null) {
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

    /**
     * Packs two 32-bit integers into a single 64-bit {@link Long} for chunk indexing.
     *
     * @param cx the chunk x coordinate
     * @param cz the chunk z coordinate
     * @return the chunk key as a 64-bit {@link Long}
     */
    @Contract(pure = true)
    private long getChunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }
}