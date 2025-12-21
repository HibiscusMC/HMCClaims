package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class ClaimRegion {

    private final String worldName;
    private final List<BlockSelection> corners;

    private int minX, maxX, minZ, maxZ;

    public ClaimRegion(String worldName) {
        this.worldName = worldName;
        this.corners = new ArrayList<>();
    }

    public ClaimRegion(String worldName, List<BlockSelection> corners) {
        this.worldName = worldName;
        this.corners = corners;
    }

    public World bukkitWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void addCorner(BlockSelection selection) {
        this.corners.add(selection);

        if (this.corners.size() == 2) {
            BlockSelection firstCorner = this.corners.getFirst();

            this.minX = Math.min(firstCorner.x(), selection.x());
            this.maxX = Math.max(firstCorner.x(), selection.x());
            this.minZ = Math.min(firstCorner.z(), selection.z());
            this.maxZ = Math.max(firstCorner.z(), selection.z());
        }
    }

    public void removeCorner(BlockSelection selection) {
        this.corners.remove(selection);
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) {
            return false;
        }

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public long getSurfaceArea() {
        long length = (maxX - minX) + 1;
        long width = (maxZ - minZ) + 1;
        return length * width;
    }

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

}