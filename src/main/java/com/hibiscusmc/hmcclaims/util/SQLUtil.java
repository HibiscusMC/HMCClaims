package com.hibiscusmc.hmcclaims.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SQLUtil {

    public static byte[] UUIDtoBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID bytesToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }

}