package com.hibiscusmc.hmcclaims.marker;

import org.bukkit.Color;

public enum MarkType {
    SELECT(255, 187, 0),
    CREATE(3, 181, 0),
    INSPECT(210, 76, 159);

    private final int r;
    private final int g;
    private final int b;

    MarkType(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color getColor() {
        return Color.fromRGB(r, g, b);
    }
}