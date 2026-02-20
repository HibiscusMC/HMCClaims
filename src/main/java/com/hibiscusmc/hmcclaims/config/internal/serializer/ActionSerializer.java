package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.gui.Action;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ActionSerializer implements TypeSerializer<Action> {

    public final static ActionSerializer INSTANCE = new ActionSerializer();

    private ActionSerializer() {
    }

    @Override
    public Action deserialize(Type type, ConfigurationNode node) {
        String rawAction = node.getString();

        if (rawAction == null) {
            return null;
        }

        return Action.parse(rawAction);
    }

    @Override
    public void serialize(Type type, @Nullable Action action, ConfigurationNode node) throws SerializationException {
        if (action == null) {
            node.raw(null);
            throw new SerializationException("Action is null");
        }

        node.set(action.rawAction());
    }
}