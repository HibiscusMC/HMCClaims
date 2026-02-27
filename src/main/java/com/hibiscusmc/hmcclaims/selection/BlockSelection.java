package com.hibiscusmc.hmcclaims.selection;

import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an immutable 2D coordinate pair (X and Z) within a world.
 */
@Data
public class BlockSelection {

    private final int x;
    private final int z;

    /**
     * Creates a BlockSelection from a Bukkit Location.
     *
     * @param loc The source location.
     * @return A new BlockSelection containing the block coordinates.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static BlockSelection fromBukkitLocation(@NotNull Location loc) {
        return new BlockSelection(
                loc.getBlockX(),
                loc.getBlockZ()
        );
    }

    /**
     * Deserializes a BlockSelection from a formatted string.
     * <p>
     * Expected format: {@code "x;z"}
     *
     * @param str The serialized coordinate string.
     * @return A new BlockSelection instance.
     * @throws NumberFormatException          if the string parts are not valid integers.
     * @throws ArrayIndexOutOfBoundsException if the string does not contain a semicolon.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static BlockSelection fromString(@NotNull String str) {
        String[] split = str.split(";");

        return new BlockSelection(
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1])
        );
    }

    /**
     * Serializes the coordinates into a string format.
     *
     * @return A string in the format {@code "x;z"}.
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        return x + ";" + z;
    }
}