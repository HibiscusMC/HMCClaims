package com.hibiscusmc.hmcclaims.util;

import com.google.protobuf.ByteString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility for converting {@link UUID}s to and from byte arrays and Protobuf {@link ByteString}s.
 * <p>
 * This is primarily used for database storage efficiency (allowing UUIDs to be
 * stored as {@code BINARY(16)}) and ultra-compact Protobuf network serialization.
 */
public class ByteUtil {

    /**
     * Converts a {@link UUID} into a 16-byte array.
     *
     * @param uuid The UUID to convert.
     * @return A byte array of length 16 representing the bits of the UUID.
     */
    @Contract(value = "_ -> new", pure = true)
    public static byte @NotNull [] UUIDtoBytes(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
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

    /**
     * Converts a {@link UUID} into a 16-byte Protobuf {@link ByteString}.
     *
     * @param uuid The UUID to convert.
     * @return A ByteString of length 16 representing the bits of the UUID, or {@link ByteString#EMPTY} if null.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ByteString UUIDtoByteString(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return ByteString.copyFrom(bb.array());
    }

    /**
     * Converts a 16-byte Protobuf {@link ByteString} back into a {@link UUID}.
     *
     * @param byteString The 16-byte structure to convert.
     * @return The resulting {@link UUID}.
     * @throws IllegalArgumentException if the ByteString is null or not exactly 16 bytes.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static UUID byteStringToUUID(@NotNull ByteString byteString) {
        if (byteString.size() != 16) {
            throw new IllegalArgumentException("Protobuf ByteString must be exactly 16 bytes to construct a valid UUID.");
        }

        ByteBuffer bb = ByteBuffer.wrap(byteString.toByteArray());
        return new UUID(bb.getLong(), bb.getLong());
    }
}