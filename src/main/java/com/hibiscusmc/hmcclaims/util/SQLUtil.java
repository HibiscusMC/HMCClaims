package com.hibiscusmc.hmcclaims.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility for converting {@link UUID}s to and from byte arrays.
 * <p>
 * This is primarily used for database storage efficiency, allowing UUIDs to be
 * stored as {@code BINARY(16)} rather than {@code VARCHAR(36)}.
 */
public class SQLUtil {

    /**
     * Converts a {@link UUID} into a 16-byte array.
     *
     * @param uuid The UUID to convert.
     * @return A byte array of length 16 representing the bits of the UUID.
     */
    @Contract(value = "_ -> new", pure = true)
    public static byte @NotNull [] UUIDtoBytes(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    /**
     * Converts a 16-byte array back into a {@link UUID}.
     *
     * @param bytes The 16-byte array to convert.
     * @return The resulting {@link UUID}.
     * @throws java.nio.BufferUnderflowException if the array is less than 16 bytes.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static UUID bytesToUUID(byte @NotNull [] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        return new UUID(bb.getLong(), bb.getLong());
    }
}