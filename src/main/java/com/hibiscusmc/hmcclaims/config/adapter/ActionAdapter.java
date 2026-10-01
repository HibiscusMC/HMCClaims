package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.gui.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.Type;

public class ActionAdapter implements Adapter<Action> {

    @Override
    public Action read(@NotNull Type type, @NotNull ConfigValue source) {
        if (source.isMap() || source.isList()) {
            throw new ConfigException(source, "Expected an action string");
        }

        String raw = String.valueOf(source.unwrap());

        try {
            return Action.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new ConfigException(source, e.getMessage() + ": " + raw, e);
        }
    }

    @Override
    public void write(@NotNull Type type, @Nullable Action value, @NotNull ConfigValue target) {
        target.set(value == null ? null : value.rawAction());
    }
}
