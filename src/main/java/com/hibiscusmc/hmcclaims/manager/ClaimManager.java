package com.hibiscusmc.hmcclaims.manager;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClaimManager {

    private final static Map<UUID, List<Claim>> PLAYER_CLAIMS
            = new ConcurrentHashMap<>();
    private final static Map<Long, List<Claim>> CHUNK_MAP
            = new ConcurrentHashMap<>();

    public Claim createClaim(Player player, ClaimRegion region) {
        Claim newClaim = new Claim(UUID.randomUUID(), player, region);

        PLAYER_CLAIMS.computeIfAbsent(player.getUniqueId(), k -> new CopyOnWriteArrayList<>())
                .add(newClaim);

        int minX = region.minX() >> 4;
        int maxX = region.maxX() >> 4;
        int minZ = region.minZ() >> 4;
        int maxZ = region.maxZ() >> 4;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = Chunk.getChunkKey(cx, cz);

                CHUNK_MAP.computeIfAbsent(chunkKey, k -> new CopyOnWriteArrayList<>())
                        .add(newClaim);
            }
        }

        return newClaim;
    }

    public void deleteClaim(Claim claim) {
        List<Claim> playerList = PLAYER_CLAIMS.get(claim.owner());
        if (playerList != null) {
            playerList.remove(claim);
        }

        ClaimRegion region = claim.region();
        int minX = region.minX() >> 4;
        int maxX = region.maxX() >> 4;
        int minZ = region.minZ() >> 4;
        int maxZ = region.maxZ() >> 4;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = Chunk.getChunkKey(cx, cz);
                List<Claim> claimsInChunk = CHUNK_MAP.get(chunkKey);

                if (claimsInChunk != null) {
                    claimsInChunk.remove(claim);
                    if (claimsInChunk.isEmpty()) {
                        CHUNK_MAP.remove(chunkKey);
                    }
                }
            }
        }
    }

    public List<Claim> getPlayerClaims(UUID uuid) {
        return PLAYER_CLAIMS.getOrDefault(uuid, Collections.emptyList());
    }

    public Optional<Claim> getClaimAt(Location loc) {
        long chunkKey = Chunk.getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        List<Claim> claimsInChunk = CHUNK_MAP.get(chunkKey);

        if (claimsInChunk == null || claimsInChunk.isEmpty()) {
            return Optional.empty();
        }

        return claimsInChunk.stream()
                .filter(claim -> claim.region().contains(loc))
                .findFirst();
    }

    public boolean isOverlapping(ClaimRegion newRegion) {
        int minX = newRegion.minX() >> 4;
        int maxX = newRegion.maxX() >> 4;
        int minZ = newRegion.minZ() >> 4;
        int maxZ = newRegion.maxZ() >> 4;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = Chunk.getChunkKey(cx, cz);
                List<Claim> claimsInChunk = CHUNK_MAP.get(chunkKey);

                if (claimsInChunk == null) continue;

                for (Claim existingClaim : claimsInChunk) {
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
}