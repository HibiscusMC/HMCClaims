package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import team.unnamed.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldListener implements Listener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    private ClaimManager claimManager;

    @Inject
    private StorageHolder storageHolder;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Storage storage = storageHolder.get();
        ClaimRepository claimRepository = storage.claims();

        World world = event.getWorld();
        String worldName = world.getName();

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ());

        executor.submit(() -> {
            Map<String, Map<Long, Set<Claim>>> worldChunks = claimManager.worldChunkMaps();
            Map<Long, Set<Claim>> chunks = worldChunks.get(worldName);

            if (chunks != null && !chunks.isEmpty()) {
                Set<Claim> claims = chunks.get(chunkKey);

                if (claims != null && !claims.isEmpty()) {
                    return;
                }
            }

            List<Claim> claimList = claimRepository.getAllClaims(chunkKey).join();
            if (claimList == null || claimList.isEmpty()) {
                return;
            }

            for (Claim claim : claimList) {
                claimManager.addClaimToCache(claim);

                for (Claim subClaim : claim.subClaims()) {
                    claimManager.addClaimToCache(subClaim);
                }
            }
        });
    }
}