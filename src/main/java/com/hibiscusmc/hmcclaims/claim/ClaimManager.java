package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

@Singleton
public class ClaimManager {

    private final Map<String, Map<Long, List<Claim>>> worldChunkMaps
            = new ConcurrentHashMap<>();

    private final Map<UUID, List<Claim>> playerClaims
            = new ConcurrentHashMap<>();

    @Inject
    private ConfigHolder<DefaultRoles> rolesHolder;

    public Claim createClaim(Player player, ClaimRegion region, @Nullable Claim parent) {
        Claim newClaim = new Claim(UUID.randomUUID(), parent, player, region, rolesHolder.get().defaultRoles());

        if (parent != null) {
            parent.addChild(newClaim);
        }

        addClaimToCache(newClaim);

        return newClaim;
    }

    public void addClaimToCache(Claim claim) {
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

    public List<Claim> getClaimsAt(Location loc) {
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

    public Optional<Claim> getClaimAt(Location loc) {
        List<Claim> claims = getClaimsAt(loc);
        if (claims.isEmpty()) {
            return Optional.empty();
        }

        return claims.stream()
                .max(Comparator.comparingInt(this::getClaimDepth));
    }

    private int getClaimDepth(Claim claim) {
        return (claim.parent() == null) ? 0 : 1;
    }

    public void deleteClaim(Claim claim) {
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

    public List<Claim> getPlayerClaims(UUID uuid) {
        return playerClaims.getOrDefault(uuid, Collections.emptyList());
    }

    public boolean isOverlapping(ClaimRegion newRegion, UUID playerId, boolean checkForChild) {
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
                    if (checkForChild && existingClaim.owner().uuid().equals(playerId) && existingClaim.parent() == null) {
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

    private boolean regionsOverlap(ClaimRegion a, ClaimRegion b) {
        if (!a.worldName().equals(b.worldName())) {
            return false;
        }

        return a.minX() <= b.maxX() && a.maxX() >= b.minX() &&
                a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }

    private long getChunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

}