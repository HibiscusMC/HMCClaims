package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.RawClaim;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class WorldListener implements Listener {

    private final Map<String, Long2ObjectMap<Set<RawClaim>>> tempChunksHolder
            = new ConcurrentHashMap<>();

    private final ExecutorService executor
            = Executors.newSingleThreadExecutor();

    @Inject
    private ClaimManager claimManager;

    @Inject
    private StorageHolder storageHolder;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();

        Storage storage = storageHolder.get();

        storage.claims().getAllClaims(worldName).thenAccept(claims ->
                tempChunksHolder.put(worldName, claims)
        ).join();

        Logger.log("Loaded " + tempChunksHolder.get(worldName).size() + " chunks for world " + worldName);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();

        Set<Claim> claims = claimManager.worldClaims()
                .getOrDefault(world.getName(), Set.of());

        saveClaims(claims, true);
        tempChunksHolder.remove(world.getName());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        String worldName = event.getWorld().getName();
        Chunk chunk = event.getChunk();
        long chunkKey = ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ());

        Long2ObjectMap<Set<RawClaim>> chunksHolder = tempChunksHolder.get(worldName);
        if (chunksHolder == null) {
            return;
        }

        Set<RawClaim> chunkClaims = chunksHolder.get(chunkKey);
        if (chunkClaims == null) {
            return;
        }

        Set<RawClaim> claimsToLoad = ConcurrentHashMap.newKeySet();
        for (RawClaim rawClaim : chunkClaims) {
            if (claimManager.isClaimLoading(rawClaim.claimId())) {
                continue;
            }

            claimManager.addPendingClaim(rawClaim.claimId());
            claimsToLoad.add(rawClaim);
        }

        if (claimsToLoad.isEmpty()) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {
                    Set<Claim> claims = ConcurrentHashMap.newKeySet();

                    for (RawClaim rawClaim : claimsToLoad) {
                        Claim claim = rawClaim.inflate(null);

                        claimManager.addClaimToCache(claim);

                        for (Claim subClaim : claim.subClaims()) {
                            claimManager.addClaimToCache(subClaim);
                        }
                    }

                    return claims;
                }, executor)
                .whenComplete((claims, throwable) -> {
                    if (throwable != null) {
                        for (RawClaim claimId : claimsToLoad) {
                            claimManager.removePendingClaim(claimId.claimId());
                        }

                        Logger.error("Something went wrong while loading chunk " + chunk, throwable);
                        return;
                    }

                    chunksHolder.remove(chunkKey);

                    for (Claim claim : claims) {
                        claimManager.removePendingClaim(claim.claimId());

                        for (long claimChunkKey : claim.chunks()) {
                            if (claimChunkKey == chunkKey) {
                                continue;
                            }

                            Set<RawClaim> unloadedClaims = chunksHolder.get(claimChunkKey);
                            if (unloadedClaims == null) {
                                continue;
                            }

                            unloadedClaims.removeIf(unloadedClaim -> unloadedClaim.claimId().equals(claim.claimId()));

                            synchronized (chunksHolder) {
                                if (unloadedClaims.isEmpty()) {
                                    chunksHolder.remove(claimChunkKey);
                                }
                            }
                        }
                    }
                });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        World world = event.getWorld();
        String worldName = event.getWorld().getName();
        Chunk chunk = event.getChunk();
        long chunkKey = ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ());

        Long2ObjectMap<Set<Claim>> chunks = claimManager.worldChunkMaps().get(worldName);
        if (chunks == null) {
            return;
        }

        Set<Claim> claims = chunks.get(chunkKey);
        if (claims == null || claims.isEmpty()) {
            chunks.remove(chunkKey);
            return;
        }

        Set<Claim> claimsToUnload = new HashSet<>();
        for (Claim claim : claims) {
            boolean shouldUnload = true;

            for (long claimChunkKey : claim.chunks()) {
                ChunkUtil.ChunkHolder claimChunk = ChunkUtil.fromChunkKey(claimChunkKey);

                if (world.isChunkLoaded(claimChunk.x(), claimChunk.z())) {
                    shouldUnload = false;
                    break;
                }
            }

            if (!shouldUnload) {
                continue;
            }

            Long2ObjectMap<Set<RawClaim>> chunksHolder = tempChunksHolder.computeIfAbsent(worldName, k ->
                    Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
            );

            for (long claimChunkKey : claim.chunks()) {
                Set<RawClaim> claimDataSet = chunksHolder.computeIfAbsent(claimChunkKey, k -> ConcurrentHashMap.newKeySet());
                claimDataSet.add(claim.deflate());
            }

            claimsToUnload.add(claim);
        }

        saveClaims(claimsToUnload, false);
    }

    /**
     * Saves the claims sequentially to avoid database locks.
     *
     * @param claims The list of claims to save.
     * @param sync   If the execution should lock the main thread or an asynchronous thread.
     */
    private void saveClaims(@NotNull Set<Claim> claims, boolean sync) {
        Runnable unload = () -> {
            Storage storage = storageHolder.get();

            for (Claim claim : claims) {
                claimManager.removeClaimFromCache(claim);

                storage.claims().saveClaim(claim)
                        .join();
            }
        };

        if (sync) {
            unload.run();
        } else {
            CompletableFuture.runAsync(unload, executor);
        }
    }
}