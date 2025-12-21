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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class MarkedBlock {

    private final static Transformation TRANSFORMATION
            = new Transformation(
            new Vector3f(-0.01f, -0.01f, -0.01f),
            new AxisAngle4f(0, 0, 0, 0),
            new Vector3f(1.02f, 1.02f, 1.02f),
            new AxisAngle4f(0, 0, 0, 0)
    );
    private final static Display.Brightness BRIGHTNESS
            = new Display.Brightness(0, 0);

    private BlockDisplay blockDisplay;

    private final BlockSelection blockSelection;
    private final Plugin plugin;

    public MarkedBlock(BlockSelection blockSelection, BlockDisplay blockDisplay, Plugin plugin) {
        this.blockSelection = blockSelection;
        this.blockDisplay = blockDisplay;
        this.plugin = plugin;
    }

    public BlockSelection blockSelection() {
        return blockSelection;
    }

    public void mark(Player player) {
        player.showEntity(plugin, blockDisplay);
    }

    public void hide(Player player) {
        player.hideEntity(plugin, blockDisplay);
    }

    public void destroy() {
        this.blockDisplay.remove();
        this.blockDisplay = null;
    }

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

        return new MarkedBlock(block, blockDisplay, plugin);
    }
}