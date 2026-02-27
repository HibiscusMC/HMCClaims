package com.hibiscusmc.hmcclaims.selection;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link BlockSelection} that includes the vertical (Y) axis.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BlockSelectionWithY extends BlockSelection {

    private final int y;

    /**
     * Constructs a 3D block coordinate.
     *
     * @param x The X coordinate.
     * @param y The Y (vertical) coordinate.
     * @param z The Z coordinate.
     */
    public BlockSelectionWithY(int x, int y, int z) {
        super(x, z);

        this.y = y;
    }

    /**
     * Creates a 3D selection from a Bukkit Location.
     *
     * @param loc The source location.
     * @return A new 3D coordinate instance.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static BlockSelectionWithY fromBukkitLocation(@NotNull Location loc) {
        return new BlockSelectionWithY(
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    /**
     * Gets the vertical coordinate.
     *
     * @return The Y-axis value.
     */
    @Contract(pure = true)
    public int y() {
        return y;
    }
}