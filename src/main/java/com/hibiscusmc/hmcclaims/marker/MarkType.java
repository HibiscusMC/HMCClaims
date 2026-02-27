package com.hibiscusmc.hmcclaims.marker;

import org.bukkit.Color;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the visual color schemes used for world markers and claim boundaries.
 */
public enum MarkType {
    /**
     * Gold: Used for the primary selection corners of a new top-level claim.
     */
    SELECT(255, 187, 0),

    /**
     * Light Yellow: Used for the selection corners of a sub-claim.
     */
    SELECT_SUB(255, 211, 92),

    /**
     * Green: Indicates a successful creation of a personal claim.
     */
    CREATE(3, 181, 0),

    /**
     * Lime: Indicates a successful creation of a personal sub-claim.
     */
    CREATE_SUB(95, 255, 92),

    /**
     * Magenta/Pink: Used when the owner inspects their own top-level claim.
     */
    INSPECT(210, 76, 159),

    /**
     * Light Pink: Used when the owner inspects their own sub-claim.
     */
    INSPECT_SUB(255, 89, 136),

    /**
     * Dark Red: Indicates a claim owned by another player.
     */
    INSPECT_OTHER(168, 0, 0),

    /**
     * Bright Red: Indicates a sub-claim owned by another player.
     */
    INSPECT_SUB_OTHER(255, 0, 0);

    private final int r;
    private final int g;
    private final int b;

    MarkType(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Converts the internal RGB values into a Bukkit Color object.
     *
     * @return A {@link Color} instance for use the block displays glow override color
     */
    @NotNull
    @Contract(pure = true)
    public Color getColor() {
        return Color.fromRGB(r, g, b);
    }
}