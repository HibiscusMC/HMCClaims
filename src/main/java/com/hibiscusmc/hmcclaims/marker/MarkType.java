package com.hibiscusmc.hmcclaims.marker;

import org.bukkit.Color;

public enum MarkType {
    SELECT(255, 187, 0),
    SELECT_CHILD(255, 211, 92),
    CREATE(3, 181, 0),
    CREATE_CHILD(95, 255, 92),
    INSPECT(210, 76, 159),
    INSPECT_CHILD(255, 89, 136),
    INSPECT_OTHER(168, 0, 0),
    INSPECT_CHILD_OTHER(255, 0, 0);

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