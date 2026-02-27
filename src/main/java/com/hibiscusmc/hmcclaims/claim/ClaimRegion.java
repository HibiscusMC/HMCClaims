package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the physical 2D boundaries of a claim within a specific world.
 */
@Getter
@ToString
public class ClaimRegion {

    private final String worldName;
    private final List<BlockSelection> corners;

    private int minX, maxX, minZ, maxZ;

    public ClaimRegion(String worldName) {
        this.worldName = worldName;
        this.corners = new ArrayList<>();
    }

    /**
     * Constructs a region and immediately calculates bounds if two corners are present.
     */
    public ClaimRegion(String worldName, List<BlockSelection> corners) {
        this.worldName = worldName;
        this.corners = corners;

        if (this.corners.size() == 2) {
            calculateCorners();
        }
    }

    /**
     * Resolves the world name to a Bukkit {@link World} instance.
     *
     * @return The world, or {@code null} if it is currently unloaded.
     */
    @Contract(pure = true)
    public World bukkitWorld() {
        return Bukkit.getWorld(worldName);
    }

    /**
     * Adds a defining corner to the region. Triggers bounds recalculation on the second corner.
     *
     * @param selection The block coordinate selection.
     */
    public void addCorner(BlockSelection selection) {
        this.corners.add(selection);

        if (this.corners.size() == 2) {
            calculateCorners();
        }
    }

    /**
     * Removes a corner from the region
     *
     * @param selection The block coordinate selection.
     */
    public void removeCorner(BlockSelection selection) {
        this.corners.remove(selection);
    }

    /**
     * Checks if the specified block location is a corner of the region
     *
     * @param selection the block location to check
     * @return {@code true} if it's a corner of the region
     */
    @Contract(pure = true)
    public boolean isCorner(@NotNull BlockSelection selection) {
        return (selection.x() == minX || selection.x() == maxX) &&
                (selection.z() == minZ || selection.z() == maxZ);
    }

    /**
     * Checks if a specific location falls within the claim boundaries.
     *
     * @param loc The location to check.
     * @return {@code true} if the location is within the X/Z bounds in the same world.
     */
    @Contract(pure = true)
    public boolean contains(@NotNull Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) {
            return false;
        }

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    /**
     * Calculates the total 2D area (X * Z) of the region.
     *
     * @return Total surface blocks.
     */
    @Contract(pure = true)
    public long getSurfaceArea() {
        long length = (maxX - minX) + 1;
        long width = (maxZ - minZ) + 1;

        return length * width;
    }

    /**
     * Generates a list of coordinates forming L-shaped markers at each of the four corners.
     *
     * @return A list of block coordinates to be visually marked.
     */
    @NotNull
    @Contract(pure = true)
    public List<BlockSelection> getLCornerBlocks() {
        if (corners.size() < 2) {
            return List.of();
        }

        BlockSelection firstCorner = corners.getFirst();
        BlockSelection secondCorner = corners.getLast();

        int minX = Math.min(firstCorner.x(), secondCorner.x());
        int maxX = Math.max(firstCorner.x(), secondCorner.x());
        int minZ = Math.min(firstCorner.z(), secondCorner.z());
        int maxZ = Math.max(firstCorner.z(), secondCorner.z());

        List<BlockSelection> blocks = new ArrayList<>();

        int[][] corners = {
                {minX, minZ}, {minX, maxZ},
                {maxX, minZ}, {maxX, maxZ}
        };

        for (int[] corner : corners) {
            int cx = corner[0];
            int cz = corner[1];

            blocks.add(new BlockSelection(cx, cz));

            int offsetX = (cx == minX) ? 1 : -1;
            int offsetZ = (cz == minZ) ? 1 : -1;

            if (maxX > minX) blocks.add(new BlockSelection(cx + offsetX, cz));
            if (maxZ > minZ) blocks.add(new BlockSelection(cx, cz + offsetZ));
        }

        return blocks;
    }

    /**
     * Normalizes the two input corners into min/max bounds for efficient lookup.
     */
    private void calculateCorners() {
        BlockSelection firstCorner = this.corners.getFirst();
        BlockSelection secondCorner = this.corners.getLast();

        this.minX = Math.min(firstCorner.x(), secondCorner.x());
        this.maxX = Math.max(firstCorner.x(), secondCorner.x());
        this.minZ = Math.min(firstCorner.z(), secondCorner.z());
        this.maxZ = Math.max(firstCorner.z(), secondCorner.z());
    }
}