package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.RawClaim;
import com.hibiscusmc.hmcclaims.claim.UnloadedClaims;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import com.hibiscusmc.hmcclaims.util.ExceptionUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
public class ClaimLifecycleListener implements Listener {

    private static final int STARTUP_ATTEMPTS = 3;
    private static final long RETRY_DELAY_SECONDS = 3;

    private final Set<String> indexedWorlds
            = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor
            = Executors.newVirtualThreadPerTaskExecutor();

    private final ClaimManager claimManager;

    private final StorageHolder storageHolder;

    private final UnloadedClaims unloadedClaims;

    private volatile boolean started;

    @Inject
    public ClaimLifecycleListener(ClaimManager claimManager, StorageHolder storageHolder, UnloadedClaims unloadedClaims, SchedulerUtil scheduler) {
        this.claimManager = claimManager;
        this.storageHolder = storageHolder;
        this.unloadedClaims = unloadedClaims;

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

                    if (claim.main() != null) {
                        continue;
                    }

                    if (System.currentTimeMillis() - claim.ttl() < TimeUnit.MINUTES.toMillis(1)) {
                        continue;
                    }

                    if (!isUnloadable(claim)) {
                        continue;
                    }

                    claimsToUnload.add(claim);
                }

                if (claimsToUnload.isEmpty()) {
                    return;
                }

                unloadClaims(claimsToUnload, false);
            }
        }, 5L);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }

        started = true;

        Storage storage = storageHolder.get();
        List<String> failedWorlds = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();

            long start = System.currentTimeMillis();
            RawClaim.CacheHolder holder = loadWorldClaims(storage, worldName);

            if (holder == null) {
                failedWorlds.add(worldName);
                continue;
            }

            indexWorld(worldName, holder);
            loadLoadedChunks(world);

            long end = System.currentTimeMillis() - start;

            Logger.log("Loaded " + holder.chunks().size() + " chunks for world '" + worldName + "' in " + end + "ms");
        }

        if (!failedWorlds.isEmpty()) {
            Logger.error("Couldn't load the claims of world(s) {}. Shutting down the server so they aren't left unprotected.", String.join(", ", failedWorlds));
            Bukkit.shutdown();
        }
    }

    @Nullable
    private RawClaim.CacheHolder loadWorldClaims(@NotNull Storage storage, @NotNull String worldName) {
        for (int attempt = 1; attempt <= STARTUP_ATTEMPTS; attempt++) {
            try {
                return storage.claims().getAllClaims(worldName).join();
            } catch (CompletionException | CancellationException ex) {
                if (attempt == STARTUP_ATTEMPTS) {
                    Logger.error("Couldn't load the claims of world '" + worldName + "'", ex);

                    return null;
                }

                Logger.warning("Couldn't load the claims of world '{}' (attempt {}/{}): {}. Retrying in {}s...",
                        worldName, attempt, STARTUP_ATTEMPTS, ExceptionUtil.rootMessage(ex), RETRY_DELAY_SECONDS);

                try {
                    TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    Logger.error("Couldn't load the claims of world '" + worldName + "'", ex);

                    return null;
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!started) {
            return;
        }

        World world = event.getWorld();
        String worldName = world.getName();

        if (indexedWorlds.contains(worldName)) {
            loadLoadedChunks(world);
            return;
        }

        RawClaim.CacheHolder holder = loadWorldClaims(storageHolder.get(), worldName);
        if (holder == null) {
            Logger.error("Couldn't load the claims of world '{}', they won't be protected until it's loaded again", worldName);
            return;
        }

        indexWorld(worldName, holder);
        loadLoadedChunks(world);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Set<Claim> claims = new HashSet<>();

        for (Claim claim : claimManager.worldClaims().getOrDefault(event.getWorld().getName(), Set.of())) {
            if (claim.main() == null) {
                claims.add(claim);
            }
        }

        unloadClaims(claims, true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        String worldName = event.getWorld().getName();
        Chunk chunk = event.getChunk();
        long chunkKey = ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ());

        Long2ObjectMap<Set<RawClaim>> chunksHolder = unloadedClaims.chunks().get(worldName);
        if (chunksHolder == null) {
            return;
        }

        Set<RawClaim> chunkClaims = chunksHolder.get(chunkKey);
        if (chunkClaims == null) {
            return;
        }

        loadClaims(chunkClaims);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        Set<RawClaim> rawClaims = unloadedClaims.players().get(playerId);
        if (rawClaims == null || rawClaims.isEmpty()) {
            unloadedClaims.players().remove(playerId);
            return;
        }

        loadClaims(rawClaims);
    }

    private boolean isUnloadable(@NotNull Claim claim) {
        if (Bukkit.getPlayer(claim.owner()) != null) {
            return false;
        }

        for (Claim subClaim : claim.subClaims()) {
            if (Bukkit.getPlayer(subClaim.owner()) != null) {
                return false;
            }
        }

        World world = claim.region().bukkitWorld();
        if (world == null) {
            return true;
        }

        for (long chunkKey : claim.chunks()) {
            ChunkUtil.ChunkHolder chunkHolder = ChunkUtil.fromChunkKey(chunkKey);

            if (world.isChunkLoaded(chunkHolder.x(), chunkHolder.z())) {
                return false;
            }
        }

        return true;
    }

    private void indexWorld(@NotNull String worldName, @NotNull RawClaim.CacheHolder holder) {
        Long2ObjectMap<Set<RawClaim>> chunks = unloadedClaims.chunks().computeIfAbsent(worldName, k ->
                Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
        );

        for (Long2ObjectMap.Entry<Set<RawClaim>> chunkEntry : holder.chunks().long2ObjectEntrySet()) {
            chunks.computeIfAbsent(chunkEntry.getLongKey(), k -> ConcurrentHashMap.newKeySet())
                    .addAll(chunkEntry.getValue());
        }

        for (Map.Entry<UUID, Set<RawClaim>> playerEntry : holder.players().entrySet()) {
            unloadedClaims.players().computeIfAbsent(playerEntry.getKey(), k -> ConcurrentHashMap.newKeySet())
                    .addAll(playerEntry.getValue());
        }

        indexedWorlds.add(worldName);
    }

    private void loadLoadedChunks(@NotNull World world) {
        Set<RawClaim> toLoad = new HashSet<>();

        for (Chunk chunk : world.getLoadedChunks()) {
            toLoad.addAll(unloadedClaims.at(world.getName(), ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ())));
        }

        loadClaims(toLoad);
    }

    // Main thread only: the caches and the unloaded index aren't touched anywhere else.
    private void loadClaims(@NotNull Collection<RawClaim> raws) {
        for (RawClaim raw : List.copyOf(raws)) {
            if (claimManager.getClaim(raw.claimId()).isPresent()) {
                unindex(raw);
                continue;
            }

            try {
                Claim claim = raw.inflate(null);

                claimManager.addClaimToCache(claim);

                for (Claim subClaim : claim.subClaims()) {
                    claimManager.addClaimToCache(subClaim);
                }
            } catch (RuntimeException ex) {
                Logger.error("Couldn't load claim " + raw.claimId(), ex);
                continue;
            }

            unindex(raw);
        }
    }

    // Main thread only: the caches and the unloaded index aren't touched anywhere else.
    private void unloadClaims(@NotNull Collection<Claim> topLevelClaims, boolean sync) {
        List<RawClaim> raws = new ArrayList<>();

        for (Claim claim : topLevelClaims) {
            RawClaim raw = claim.deflate();

            for (Claim subClaim : claim.subClaims()) {
                claimManager.removeClaimFromCache(subClaim);
            }

            claimManager.removeClaimFromCache(claim);
            index(raw);
            raws.add(raw);
        }

        if (raws.isEmpty()) {
            return;
        }

        Runnable save = () -> {
            Storage storage = storageHolder.get();

            for (RawClaim raw : raws) {
                saveRow(storage, raw);

                for (RawClaim subRaw : raw.subClaims()) {
                    saveRow(storage, subRaw);
                }
            }
        };

        if (sync) {
            save.run();
        } else {
            CompletableFuture.runAsync(save, executor);
        }
    }

    private void saveRow(@NotNull Storage storage, @NotNull RawClaim raw) {
        try {
            storage.claims().saveClaim(raw).join();
        } catch (RuntimeException ex) {
            Logger.error("Couldn't save claim " + raw.claimId() + " while unloading it", ex);
        }
    }

    private void index(@NotNull RawClaim raw) {
        unloadedClaims.players().computeIfAbsent(raw.owner(), k -> ConcurrentHashMap.newKeySet())
                .add(raw);

        Long2ObjectMap<Set<RawClaim>> chunks = unloadedClaims.chunks().computeIfAbsent(raw.worldName(), k ->
                Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
        );

        for (int x = raw.minX() >> 4; x <= raw.maxX() >> 4; x++) {
            for (int z = raw.minZ() >> 4; z <= raw.maxZ() >> 4; z++) {
                chunks.computeIfAbsent(ChunkUtil.getChunkKey(x, z), k -> ConcurrentHashMap.newKeySet())
                        .add(raw);
            }
        }
    }

    private void unindex(@NotNull RawClaim raw) {
        UUID id = raw.claimId();

        Set<RawClaim> playerClaims = unloadedClaims.players().get(raw.owner());
        if (playerClaims != null) {
            playerClaims.removeIf(r -> r.claimId().equals(id));

            if (playerClaims.isEmpty()) {
                unloadedClaims.players().remove(raw.owner());
            }
        }

        Long2ObjectMap<Set<RawClaim>> chunks = unloadedClaims.chunks().get(raw.worldName());
        if (chunks == null) {
            return;
        }

        for (int x = raw.minX() >> 4; x <= raw.maxX() >> 4; x++) {
            for (int z = raw.minZ() >> 4; z <= raw.maxZ() >> 4; z++) {
                long key = ChunkUtil.getChunkKey(x, z);
                Set<RawClaim> chunkClaims = chunks.get(key);

                if (chunkClaims == null) {
                    continue;
                }

                chunkClaims.removeIf(r -> r.claimId().equals(id));

                if (chunkClaims.isEmpty()) {
                    chunks.remove(key);
                }
            }
        }
    }
}
