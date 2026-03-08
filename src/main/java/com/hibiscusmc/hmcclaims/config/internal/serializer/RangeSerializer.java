package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.util.RangeUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class RangeSerializer implements TypeSerializer<RangeUtil> {

    public final static RangeSerializer INSTANCE = new RangeSerializer();

    private RangeSerializer() {
    }

    @Override
    public RangeUtil deserialize(Type type, ConfigurationNode node) {
        String rawRange = node.getString();

        if (rawRange == null) {
            return null;
        }

        String[] parts = rawRange.split("-");
        int from = Integer.parseInt(parts[0].trim());

        if (parts.length < 2) {
            return new RangeUtil(from, from);
        }

        int to = Integer.parseInt(parts[1].trim());

        return new RangeUtil(from, to);
    }

    @Override
    public void serialize(Type type, @Nullable RangeUtil range, ConfigurationNode node) throws SerializationException {
        if (range == null) {
            node.raw(null);
            throw new SerializationException("Range is null");
        }

        if (range.from() == range.to()) {
            node.set(range.from());
            return;
        }

        node.set(range.toString());
    }
}