package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.util.RangeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.Type;

public class RangeAdapter implements Adapter<RangeUtil> {

    @Override
    public RangeUtil read(@NotNull Type type, @NotNull ConfigValue source) {
        if (source.isMap() || source.isList()) {
            throw new ConfigException(source, "Expected a range like 19-25 or a single number");
        }

        String raw = String.valueOf(source.unwrap());
        String[] parts = raw.split("-");

        try {
            int from = Integer.parseInt(parts[0].trim());

            if (parts.length < 2) {
                return new RangeUtil(from, from);
            }

            return new RangeUtil(from, Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            throw new ConfigException(source, "Invalid range: " + raw, e);
        }
    }

    @Override
    public void write(@NotNull Type type, @Nullable RangeUtil value, @NotNull ConfigValue target) {
        if (value == null) {
            target.set(null);
            return;
        }

        if (value.from() == value.to()) {
            target.set(value.from());
            return;
        }

        target.set(value.toString());
    }
}
