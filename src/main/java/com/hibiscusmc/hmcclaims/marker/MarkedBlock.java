package com.hibiscusmc.hmcclaims.marker;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import com.hibiscusmc.hmcclaims.selection.BlockSelectionWithY;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Encapsulates a visual block marker rendered via a {@link BlockDisplay} entity.
 */
public class MarkedBlock {

    /**
     * Predetermined transformation to scale the display to 102% of a block's size.
     * This prevents texture flickering (z-fighting) with the actual block.
     */
    private final static Transformation TRANSFORMATION
            = new Transformation(
            new Vector3f(-0.01f, -0.01f, -0.01f),
            new AxisAngle4f(0, 0, 0, 0),
            new Vector3f(1.02f, 1.02f, 1.02f),
            new AxisAngle4f(0, 0, 0, 0)
    );

    /**
     * Forces the display to ignore world lighting, ensuring consistent glow brightness.
     */
    private final static Display.Brightness BRIGHTNESS
            = new Display.Brightness(0, 0);

    /**
     * Set of players that are currently looking at this marked block
     */
    private final Set<UUID> players = new CopyOnWriteArraySet<>();

    private BlockDisplay blockDisplay;

    private final Plugin plugin;

    public MarkedBlock(BlockDisplay blockDisplay, Plugin plugin) {
        this.blockDisplay = blockDisplay;
        this.plugin = plugin;
    }

    /**
     * Gets the underlying Bukkit {@link BlockDisplay} entity used for visualization.
     * <p>
     * <strong>Note:</strong> This entity is managed by the {@link MarkedBlock} instance.
     * Manual modification of the entity's lifecycle (e.g., calling {@link BlockDisplay#remove()})
     * may lead to inconsistencies in the {@link BlockMarker} cache.
     *
     * @return The active block display entity.
     */
    @Nullable
    @Contract(pure = true)
    public BlockDisplay blockDisplay() {
        return blockDisplay;
    }

    /**
     * Retrieves an unmodifiable view of all players currently observing this marked block.
     *
     * @return A thread-safe set of {@link UUID}s representing active observers.
     */
    @NotNull
    public Set<UUID> players() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Makes the marker visible to a specific player.
     *
     * @param player The player who should see this marker.
     */
    public void mark(Player player) {
        player.showEntity(plugin, blockDisplay);
        players.add(player.getUniqueId());
    }

    /**
     * Hides the marker from a specific player.
     *
     * @param player The player who should no longer see this marker.
     */
    public void hide(Player player) {
        player.hideEntity(plugin, blockDisplay);
        players.remove(player.getUniqueId());
    }

    /**
     * Permanently removes the underlying entity from the world.
     */
    public void destroy() {
        this.blockDisplay.remove();
        this.blockDisplay = null;
        this.players.clear();
    }

    /**
     * Factory method to spawn and configure a new BlockDisplay marker.
     * <p>
     * If the {@link BlockSelection} does not provide a Y coordinate,
     * the marker is placed at the highest motion-blocking block.
     *
     * @param world  The world to spawn the marker in.
     * @param block  The coordinates for the marker.
     * @param type   The {@link MarkType} defining the glow color.
     * @param plugin The plugin instance for entity visibility management.
     * @return A configured MarkedBlock instance.
     */
    public static MarkedBlock from(World world, BlockSelection block, MarkType type, Plugin plugin) {
        Location location = new Location(world,
                block.x(),
                block instanceof BlockSelectionWithY blockY ?
                        blockY.y() :
                        world.getHighestBlockYAt(block.x(), block.z(), HeightMap.MOTION_BLOCKING_NO_LEAVES),
                block.z()
        );

        BlockDisplay blockDisplay = world.spawn(location, BlockDisplay.class);

        blockDisplay.setBlock(Material.BLACK_CONCRETE.createBlockData());
        blockDisplay.setGlowColorOverride(type.getColor());
        blockDisplay.setTransformation(TRANSFORMATION);
        blockDisplay.setBrightness(BRIGHTNESS);

        blockDisplay.setVisibleByDefault(false);
        blockDisplay.setPersistent(false);
        blockDisplay.setGlowing(true);

        return new MarkedBlock(blockDisplay, plugin);
    }
}