package com.hibiscusmc.hmcclaims.util;

import lombok.Getter;

/**
 * Utility for managing ranges used in various places of the plugin.
 * e.g. Inventory slots
 */
@Getter
public class RangeUtil {

    /**
     * The starting point of the range
     */
    private final int from;
    /**
     * The end of the range
     */
    private final int to;

    /**
     * Constructs a new RangeUtil instance providing the bounds of the range.
     *
     * @param from an {@link Integer} defining the initial bound of the range (inclusive)
     * @param to   an {@link Integer} defining the final bound of the range (inclusive)
     */
    public RangeUtil(int from, int to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Returns every number between the bounds of the defined range
     *
     * @return a {@link Integer} array containing every number between {@link #from} and {@link #to}
     */
    public int[] all() {
        int[] all = new int[to - from + 1];

        for (int i = from; i <= to; i++) {
            all[i - from] = i;
        }

        return all;
    }

    /**
     * Initializes a new RangeUtil instance from a string
     *
     * @param range The range in a "from-to" format
     * @return The RangeUtil instance from the provided String
     */
    public RangeUtil of(String range) {
        String[] parts = range.split("-");
        int from = Integer.parseInt(parts[0].trim());
        int to = Integer.parseInt(parts[1].trim());

        return new RangeUtil(from, to);
    }

    @Override
    public String toString() {
        return from + "-" + to;
    }
}