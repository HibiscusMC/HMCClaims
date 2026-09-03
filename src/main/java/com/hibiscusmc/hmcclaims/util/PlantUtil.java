package com.hibiscusmc.hmcclaims.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for identifying crops, the blocks they grow as and the items used
 * to plant them.
 */
public class PlantUtil {

    /**
     * Checks whether a block is a crop covered by the planting permission.
     *
     * @param type The {@link Material} of the placed block.
     * @return {@code true} if the block is a crop or sapling.
     */
    public static boolean isPlantableBlock(@NotNull Material type) {
        return Tag.CROPS.isTagged(type)
                || Tag.SAPLINGS.isTagged(type)
                || type == Material.MELON_STEM
                || type == Material.PUMPKIN_STEM
                || type == Material.ATTACHED_MELON_STEM
                || type == Material.ATTACHED_PUMPKIN_STEM
                || type == Material.SWEET_BERRY_BUSH
                || type == Material.CAVE_VINES
                || type == Material.CAVE_VINES_PLANT
                || type == Material.NETHER_WART
                || type == Material.COCOA
                || type == Material.CHORUS_FLOWER;
    }

    /**
     * Checks whether an item plants a crop when used on a block. Seeds are
     * items rather than blocks, so their material does not match the crop
     * they place.
     *
     * @param type The {@link Material} of the item in hand.
     * @return {@code true} if using the item plants a crop.
     */
    public static boolean isPlantableItem(@NotNull Material type) {
        if (isPlantableBlock(type)) {
            return true;
        }

        return switch (type) {
            case WHEAT_SEEDS, BEETROOT_SEEDS, MELON_SEEDS, PUMPKIN_SEEDS,
                 TORCHFLOWER_SEEDS, PITCHER_POD, CARROT, POTATO, COCOA_BEANS,
                 SWEET_BERRIES, GLOW_BERRIES -> true;
            default -> false;
        };
    }

    /**
     * Checks whether a block is a crop that can be harvested, either because
     * it grows through ages or because it is a produce block.
     *
     * @param block The block being broken.
     * @return {@code true} if breaking the block harvests a crop.
     */
    public static boolean isHarvestable(@NotNull Block block) {
        Material type = block.getType();
        if (Tag.CROPS.isTagged(type)
                || type == Material.NETHER_WART
                || type == Material.COCOA
                || type == Material.SWEET_BERRY_BUSH
                || type == Material.CAVE_VINES
                || type == Material.CAVE_VINES_PLANT) {
            return block.getBlockData() instanceof Ageable;
        }

        return type == Material.MELON
                || type == Material.PUMPKIN
                || type == Material.SUGAR_CANE
                || type == Material.CACTUS
                || type == Material.BAMBOO
                || type == Material.KELP
                || type == Material.KELP_PLANT;
    }
}
