package com.hibiscusmc.hmcclaims.marker;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import com.hibiscusmc.hmcclaims.selection.BlockSelectionWithY;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages visual indicators (marks) for players.
 * <p>
 * Marks are kept as desired state and re-applied every second, so displays that died or whose
 * chunk was not loaded yet are respawned. Everything runs on the main thread.
 */
@Singleton
public class BlockMarker {

    private final Map<UUID, Map<String, Mark>> playerMarks
            = new ConcurrentHashMap<>();

    private final Map<String, MarkedBlock> blockCache
            = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final SchedulerUtil scheduler;

    private record Mark(String world, BlockSelection selection, MarkType type, long expiresAt) {
    }

    /**
     * Initializes the marker service and starts the refresh task.
     *
     * @param plugin    The plugin instance.
     * @param scheduler Utility for handling asynchronous and timed tasks.
     */
    @Inject
    public BlockMarker(Plugin plugin, SchedulerUtil scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;

        scheduler.scheduleTimer(this::refresh, 20L);
    }

    /**
     * Renders a collection of block selections to a player.
     *
     * @param player     The recipient of the visual marks.
     * @param selections The coordinates to mark.
     * @param type       The visual style (color) of the mark.
     * @param durationMs Time in milliseconds before the mark expires. Use 0 for infinite.
     */
    public void mark(Player player, Collection<BlockSelection> selections, MarkType type, long durationMs) {
        scheduler.schedule(() -> {
            Map<String, Mark> marks = playerMarks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());

            String world = player.getWorld().getName();
            long expiresAt = durationMs > 0 ? System.currentTimeMillis() + durationMs : 0L;

            for (BlockSelection selection : selections) {
                String key = generateKey(world, selection, type);
                Mark mark = new Mark(world, selection, type, expiresAt);

                marks.put(key, mark);
                show(player, key, mark);
            }
        });
    }

    /**
     * Clears every active visual mark for a specific player.
     *
     * @param uuid The UUID of the player.
     */
    public void clearAllMarks(UUID uuid) {
        scheduler.schedule(() -> {
            Map<String, Mark> marks = playerMarks.remove(uuid);
            if (marks == null) {
                return;
            }

            Player player = Bukkit.getPlayer(uuid);

            for (String key : marks.keySet()) {
                hide(player, uuid, key);
            }
        });
    }

    private void show(Player player, String key, Mark mark) {
        World world = player.getWorld();
        if (!world.getName().equals(mark.world())) {
            return;
        }

        int chunkX = mark.selection().x() >> 4;
        int chunkZ = mark.selection().z() >> 4;

        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }

        Chunk.LoadLevel level = world.getChunkAt(chunkX, chunkZ).getLoadLevel();
        if (level == Chunk.LoadLevel.INACCESSIBLE || level == Chunk.LoadLevel.UNLOADED) {
            return;
        }

        MarkedBlock block = blockCache.get(key);
        if (block == null || !block.isAlive()) {
            if (block != null) {
                block.destroy();
            }

            block = MarkedBlock.from(world, mark.selection(), mark.type(), plugin);
            blockCache.put(key, block);
        }

        if (block.players().contains(player.getUniqueId())) {
            return;
        }

        block.mark(player);
    }

    private void hide(Player player, UUID uuid, String key) {
        MarkedBlock block = blockCache.get(key);
        if (block == null) {
            return;
        }

        if (player != null) {
            block.hide(player);
        } else {
            block.forget(uuid);
        }
    }

    private void refresh() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, Map<String, Mark>>> players = playerMarks.entrySet().iterator();
        while (players.hasNext()) {
            Map.Entry<UUID, Map<String, Mark>> entry = players.next();

            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                players.remove();

                for (String key : entry.getValue().keySet()) {
                    hide(null, uuid, key);
                }

                continue;
            }

            for (Map.Entry<String, Mark> markEntry : new HashMap<>(entry.getValue()).entrySet()) {
                String key = markEntry.getKey();
                Mark mark = markEntry.getValue();

                if (mark.expiresAt() > 0 && now >= mark.expiresAt()) {
                    entry.getValue().remove(key);
                    hide(player, uuid, key);

                    continue;
                }

                show(player, key, mark);
            }
        }

        blockCache.entrySet().removeIf(entry -> {
            MarkedBlock block = entry.getValue();
            if (!block.isAlive()) {
                block.destroy();
                return true;
            }

            for (UUID uuid : block.players()) {
                if (Bukkit.getPlayer(uuid) == null) {
                    block.forget(uuid);
                }
            }

            if (block.blockDisplay().getTicksLived() < 60 * 20L) {
                return false;
            }

            if (!block.players().isEmpty()) {
                return false;
            }

            block.destroy();
            return true;
        });
    }

    private String generateKey(String world, BlockSelection selection, MarkType type) {
        String key = world + ":" + selection.x() + ":" + selection.z() + ":" + type.name();

        if (selection instanceof BlockSelectionWithY withY) {
            return key + ":" + withY.y();
        }

        return key;
    }
}
