package com.hibiscusmc.hmcclaims.marker;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages visual indicators (marks) for players.
 */
@Singleton
public class BlockMarker {

    /**
     * Maps a unique string key to a specific MarkedBlock instance for caching.
     */
    private final Map<String, MarkedBlock> blockCache = new ConcurrentHashMap<>();

    /**
     * Tracks all active visual marks currently visible to a specific player.
     */
    private final Map<UUID, Set<MarkedBlock>> playerMarks = new ConcurrentHashMap<>();

    /**
     * Tracks the expiration timestamps for specific player-block marker pairs.
     */
    private final Map<MarkKey, Long> expiryMap = new ConcurrentHashMap<>();

    /**
     * Internal key used to identify a specific mark for expiration tracking.
     */
    private record MarkKey(UUID uuid, MarkedBlock block) {
    }

    private final Plugin plugin;
    private final SchedulerUtil scheduler;

    /**
     * Initializes the marker service and starts the expiration cleanup task.
     *
     * @param plugin    The plugin instance.
     * @param scheduler Utility for handling asynchronous and timed tasks.
     */
    @Inject
    public BlockMarker(Plugin plugin, SchedulerUtil scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;

        scheduler.scheduleTimer(() -> {
            long now = System.currentTimeMillis();

            expiryMap.entrySet().removeIf(entry -> {
                MarkKey key = entry.getKey();
                if (key.block().blockDisplay() == null) {
                    return true;
                }

                if (now >= entry.getValue()) {
                    removeMarkedBlock(key.uuid(), key.block());
                    return true;
                }

                return false;
            });

            blockCache.entrySet().removeIf(entry -> {
                MarkedBlock block = entry.getValue();
                if (block.blockDisplay() == null) {
                    return true;
                }

                long ticksLived = block.blockDisplay().getTicksLived();

                if (ticksLived < 60 * 20L) {
                    return false;
                }

                if (!block.players().isEmpty()) {
                    return false;
                }

                block.destroy();
                return true;
            });
        }, 5 * 20L);
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
            UUID uuid = player.getUniqueId();
            Set<MarkedBlock> activeMarks = playerMarks.computeIfAbsent(uuid, k -> new CopyOnWriteArraySet<>());

            for (BlockSelection selection : selections) {
                String cacheKey = generateKey(player.getWorld(), selection, type);
                MarkedBlock markedBlock = blockCache.computeIfAbsent(cacheKey,
                        key -> MarkedBlock.from(player.getWorld(), selection, type, plugin));

                markedBlock.mark(player);
                activeMarks.add(markedBlock);

                if (durationMs > 0) {
                    expiryMap.put(new MarkKey(uuid, markedBlock), System.currentTimeMillis() + durationMs);
                }
            }
        });
    }

    /**
     * Removes a specific marked block from a player's view and internal tracking.
     *
     * @param uuid        The UUID of the player.
     * @param markedBlock The specific mark to hide.
     */
    public void removeMarkedBlock(UUID uuid, MarkedBlock markedBlock) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            markedBlock.hide(player);
        }

        Set<MarkedBlock> set = playerMarks.get(uuid);
        if (set != null) {
            set.remove(markedBlock);
        }

        expiryMap.remove(new MarkKey(uuid, markedBlock));
    }

    /**
     * Clears every active visual mark for a specific player.
     *
     * @param uuid The UUID of the player.
     */
    public void clearAllMarks(UUID uuid) {
        scheduler.schedule(() -> {
            Set<MarkedBlock> marks = playerMarks.remove(uuid);
            if (marks != null) {
                Player player = Bukkit.getPlayer(uuid);

                marks.forEach(mark -> {
                    if (player != null) {
                        mark.hide(player);
                    }

                    expiryMap.remove(new MarkKey(uuid, mark));
                });

                marks.clear();
            }
        });
    }

    /**
     * Generates a unique cache key based on location and visual type.
     */
    private String generateKey(World world, BlockSelection sel, MarkType type) {
        return world.getName() + ":" + sel.x() + ":" + sel.z() + ":" + type.name();
    }
}