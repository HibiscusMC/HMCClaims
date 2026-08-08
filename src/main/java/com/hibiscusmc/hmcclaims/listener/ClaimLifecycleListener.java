package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.RawClaim;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
public class ClaimLifecycleListener implements Listener {

    private final Map<String, Long2ObjectMap<Set<RawClaim>>> tempChunksHolder
            = new ConcurrentHashMap<>();

    private final Map<UUID, Set<RawClaim>> tempPlayersHolder
            = new ConcurrentHashMap<>();

    private final Set<UUID> claimsUnloading
            = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor
            = Executors.newVirtualThreadPerTaskExecutor();

    private final ClaimManager claimManager;

    private final StorageHolder storageHolder;

    @Inject
    public ClaimLifecycleListener(ClaimManager claimManager, StorageHolder storageHolder, SchedulerUtil scheduler) {
        this.claimManager = claimManager;
        this.storageHolder = storageHolder;

        scheduler.scheduleTimer(new Runnable() {
            private final static int BATCH_SIZE = 100;
            private Iterator<Claim> iterator;

            @Override
            public void run() {
                if (iterator == null || !iterator.hasNext()) {
                    Map<UUID, Claim> claims = claimManager.claims();
                    if (claims.isEmpty()) {
                        return;
                    }

                    iterator = claims.values().iterator();
                }

                int checked = 0;

                Set<Claim> claimsToUnload = new HashSet<>();
                while (iterator.hasNext() && checked++ < BATCH_SIZE) {
                    Claim claim = iterator.next();

                    if (System.currentTimeMillis() - claim.ttl() < TimeUnit.MINUTES.toMillis(1)) {
                        continue;
                    }

                    if (claimsUnloading.contains(claim.claimId())) {
                        continue;
                    }

                    Player owner = Bukkit.getPlayer(claim.owner());
                    if (owner != null) {
                        continue;
                    }

                    World world = claim.region().bukkitWorld();
                    boolean shouldUnload = true;
                    for (long chunkKey : claim.chunks()) {
                        ChunkUtil.ChunkHolder chunkHolder = ChunkUtil.fromChunkKey(chunkKey);

                        if (world.isChunkLoaded(chunkHolder.x(), chunkHolder.z())) {
                            shouldUnload = false;
                            break;
                        }
                    }

                    if (!shouldUnload) {
                        continue;
                    }

                    claimsToUnload.add(claim);
                    claimsUnloading.add(claim.claimId());
                }

                if (claimsToUnload.isEmpty()) {
                    return;
                }

                saveClaims(claimsToUnload, false);
            }
        }, 5L);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();

            Storage storage = storageHolder.get();

            long start = System.currentTimeMillis();
            storage.claims().getAllClaims(worldName).thenAccept(holder -> {
                        tempChunksHolder.put(worldName, holder.chunks());

                        for (Map.Entry<UUID, Set<RawClaim>> playerEntry : holder.players().entrySet()) {
                            tempPlayersHolder.computeIfAbsent(playerEntry.getKey(), k -> ConcurrentHashMap.newKeySet())
                                    .addAll(playerEntry.getValue());
                        }
                    }
            ).join();
            long end = System.currentTimeMillis() - start;

            Logger.log("Loaded " + tempChunksHolder.get(worldName).size() + " chunks for world '" + worldName + "' in " + end + "ms");
        }
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

        loadClaims(chunkClaims);
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();

        Set<RawClaim> rawClaims = tempPlayersHolder.get(playerId);
        if (rawClaims == null || rawClaims.isEmpty()) {
            tempPlayersHolder.remove(playerId);
            return;
        }

        loadClaims(rawClaims);
    }

    /**
     * Asynchronously loads, inflates, and caches a set of raw claims.
     * <p>
     * This method filters out claims that are already in the process of loading,
     * marks the remaining claims as pending, and then processes them asynchronously
     * using the configured {@link #executor}.
     * </p>
     * <p>
     * <b>Asynchronous Lifecycle:</b>
     * <ul>
     * <li><b>Inflation:</b> Each raw claim is inflated into a full {@link Claim} object.</li>
     * <li><b>Caching:</b> Both the parent claims and any nested sub-claims are registered
     * into the {@code claimManager} cache.</li>
     * <li><b>Cleanup:</b> Upon successful processing, the claims are removed from the pending status,
     * and their tracking references are purged from both the {@code tempPlayersHolder} and
     * {@code tempChunksHolder} temporary maps.</li>
     * <li><b>Error Handling:</b> If an exception occurs during the asynchronous task, all
     * claims targeted in this batch are stripped of their pending status to allow for future
     * retry attempts, and the error is logged.</li>
     * </ul>
     * </p>
     *
     * @param claims a {@link Set} of {@link RawClaim} objects to be processed; must not be null
     * @see ClaimManager#addPendingClaim(UUID)
     * @see ClaimManager#addClaimToCache(Claim)
     * @see ClaimManager#removePendingClaim(UUID)
     */
    private void loadClaims(@NotNull Set<RawClaim> claims) {
        Set<RawClaim> claimsToLoad = ConcurrentHashMap.newKeySet();
        for (RawClaim rawClaim : claims) {
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
                    Set<Claim> claimsSet = ConcurrentHashMap.newKeySet();

                    for (RawClaim rawClaim : claimsToLoad) {
                        Claim claim = rawClaim.inflate(null);

                        claimsSet.add(claim);
                        claimManager.addClaimToCache(claim);

                        for (Claim subClaim : claim.subClaims()) {
                            claimManager.addClaimToCache(subClaim);
                        }
                    }

                    return claimsSet;
                }, executor)
                .whenComplete((claimsSet, throwable) -> {
                    if (throwable != null) {
                        for (RawClaim claimId : claimsToLoad) {
                            claimManager.removePendingClaim(claimId.claimId());
                        }

                        Logger.error("Something went wrong while loading the claims", throwable);
                        return;
                    }

                    for (Claim claim : claimsSet) {
                        claimManager.removePendingClaim(claim.claimId());

                        Set<RawClaim> playerClaims = tempPlayersHolder.get(claim.owner());
                        playerClaims.removeIf(playerClaim -> playerClaim.claimId().equals(claim.claimId()));

                        if (playerClaims.isEmpty()) {
                            tempPlayersHolder.remove(claim.owner());
                        }

                        Long2ObjectMap<Set<RawClaim>> chunksHolder = tempChunksHolder.get(claim.region().worldName());

                        for (long claimChunkKey : claim.chunks()) {
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

                        if (chunksHolder.isEmpty()) {
                            tempChunksHolder.remove(claim.region().worldName());
                        }
                    }
                });
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
                storage.claims().saveClaim(claim)
                        .join();

                RawClaim rawClaim = claim.deflate();
                Long2ObjectMap<Set<RawClaim>> chunks = tempChunksHolder.computeIfAbsent(claim.region().worldName(), (k) ->
                        Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
                );

                tempPlayersHolder.computeIfAbsent(claim.owner(), k -> ConcurrentHashMap.newKeySet())
                        .add(rawClaim);

                for (long claimChunkKey : claim.chunks()) {
                    chunks.computeIfAbsent(claimChunkKey, k -> ConcurrentHashMap.newKeySet())
                            .add(rawClaim);
                }

                claimManager.removeClaimFromCache(claim);
                claimsUnloading.remove(claim.claimId());
            }
        };

        if (sync) {
            unload.run();
        } else {
            CompletableFuture.runAsync(unload, executor);
        }
    }
}