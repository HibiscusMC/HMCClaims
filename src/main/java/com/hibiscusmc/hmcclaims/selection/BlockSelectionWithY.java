package com.hibiscusmc.hmcclaims.selection;

import org.bukkit.Location;

public class BlockSelectionWithY extends BlockSelection {

    private final int y;

    public BlockSelectionWithY(int x, int y, int z) {
        super(x, z);

        this.y = y;
    }

    public static BlockSelectionWithY fromBukkitLocation(Location loc) {
        return new BlockSelectionWithY(
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    public int y() {
        return y;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}