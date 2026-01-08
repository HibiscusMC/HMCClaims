package com.hibiscusmc.hmcclaims.selection;

import lombok.Data;
import org.bukkit.Location;

@Data
public class BlockSelection {

    private final int x;
    private final int z;

    public static BlockSelection fromBukkitLocation(Location loc) {
        return new BlockSelection(
                loc.getBlockX(),
                loc.getBlockZ()
        );
    }

    public static BlockSelection fromString(String str) {
        String[] split = str.split(";");

        return new BlockSelection(Integer.parseInt(split[0]), Integer.parseInt(split[2]));
    }

    @Override
    public String toString() {
        return x + ";" + z;
    }
}