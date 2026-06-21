package com.hibiscusmc.hmcclaims.util;

import org.jetbrains.annotations.Contract;

public class ChunkUtil {
    /**
     * Packs two 32-bit integers into a single 64-bit {@link Long} for chunk indexing.
     *
     * @param cx the chunk x coordinate
     * @param cz the chunk z coordinate
     * @return the chunk key as a 64-bit {@link Long}
     */
    @Contract(pure = true)
    public static long getChunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }
}