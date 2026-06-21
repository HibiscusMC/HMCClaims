package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the physical 2D boundaries of a claim within a specific world.
 */
@Getter
public class ClaimRegion {

    private final String worldName;
    private final List<BlockSelection> corners;

    private int minX, maxX, minZ, maxZ;

    /**
     * Constructs a region without a set of corners predefined.
     */
    public ClaimRegion(String worldName) {
        this(worldName, new ArrayList<>());
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
     * Constructs a region with a predefined set of corners.
     */
    public ClaimRegion(String worldName, int minX, int maxX, int minZ, int maxZ) {
        this.worldName = worldName;

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        this.corners = new ArrayList<>();
        corners.add(new BlockSelection(minX, minZ));
        corners.add(new BlockSelection(maxX, maxZ));
    }

    /**
     * Resizes the claim region along a horizontal direction.
     *
     * @param face   The horizontal BlockFace to move (NORTH, SOUTH, EAST, WEST).
     * @param amount The number of blocks to move the face. Positive expands, negative shrinks.
     * @return {@code true} if the claim was resized successfully, {@code false} otherwise.
     * @throws IllegalArgumentException If an invalid face is provided or if resizing makes the region invalid.
     */
    public boolean resize(@NotNull BlockFace face, int amount) {
        if (amount <= 0) {
            return false;
        }

        switch (face) {
            case NORTH -> this.minZ -= amount;
            case SOUTH -> this.maxZ += amount;
            case EAST -> this.maxX += amount;
            case WEST -> this.minX -= amount;
            default -> {
                return false;
            }
        }

        if (this.minX > this.maxX || this.minZ > this.maxZ) {
            return false;
        }

        this.corners.clear();
        this.corners.add(new BlockSelection(this.minX, this.minZ));
        this.corners.add(new BlockSelection(this.maxX, this.maxZ));

        return true;
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
     * Adds a defining corner to the region. Triggers recalculation of bounds on the second corner.
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
     * Finds the diagonally opposite corner of a given corner.
     *
     * @param corner The current selected corner.
     * @return The diagonally opposite {@link BlockSelection}.
     * @throws IllegalArgumentException If the provided block selection is not a valid corner.
     */
    @NotNull
    @Contract(pure = true)
    public BlockSelection getOppositeCorner(@NotNull BlockSelection corner) {
        if (!isCorner(corner)) {
            throw new IllegalArgumentException("Provided block selection is not a corner of this region.");
        }

        // Determine the opposite coordinate based on current bounds
        int oppositeX = (corner.x() == minX) ? maxX : minX;
        int oppositeZ = (corner.z() == minZ) ? maxZ : minZ;

        return new BlockSelection(oppositeX, oppositeZ);
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

    /**
     * Serializes the region corners into a format ready to be stored.
     * <p>
     * <b>IMPORTANT:</b> This method does <b>NOT</b> return the world name. It should be stored separate from the coordinates.
     *
     * @return The formatted corners of the region
     */
    @Override
    public String toString() {
        return minX + ";" + maxX + ";" + minZ + ";" + maxZ;
    }
}