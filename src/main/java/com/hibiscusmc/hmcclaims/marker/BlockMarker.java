package com.hibiscusmc.hmcclaims.marker;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import com.hibiscusmc.hmcclaims.util.Scheduler;
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

@Singleton
public class BlockMarker {

    private final Map<String, MarkedBlock> blockCache = new ConcurrentHashMap<>();
    private final Map<UUID, Set<MarkedBlock>> playerMarks = new ConcurrentHashMap<>();

    private final Map<MarkKey, Long> expiryMap = new ConcurrentHashMap<>();

    private record MarkKey(UUID uuid, MarkedBlock block) {
    }

    private final Plugin plugin;
    private final Scheduler scheduler;

    @Inject
    public BlockMarker(Plugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;

        scheduler.scheduleTimer(() -> {
            long now = System.currentTimeMillis();

            expiryMap.forEach((key, expiry) -> {
                if (now >= expiry) {
                    removeMarkedBlock(key.uuid(), key.block());
                    expiryMap.remove(key);
                }
            });
        }, 100L);
    }

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

    public void clearAllMarks(UUID uuid) {
        scheduler.schedule(() -> {
            Set<MarkedBlock> marks = playerMarks.remove(uuid);
            if (marks != null) {
                Player player = Bukkit.getPlayer(uuid);
                marks.forEach(m -> {
                    if (player != null) m.hide(player);
                    expiryMap.remove(new MarkKey(uuid, m));
                });
                marks.clear();
            }
        });
    }

    private String generateKey(World world, BlockSelection sel, MarkType type) {
        return world.getName() + ":" + sel.x() + ":" + sel.z() + ":" + type.name();
    }
}