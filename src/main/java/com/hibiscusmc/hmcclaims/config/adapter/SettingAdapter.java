package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SettingAdapter implements Adapter<Setting<?>> {

    /**
     * {@code Setting} is parameterized, so it can't be registered by class.
     */
    public static boolean matches(@NotNull Type type) {
        Type raw = type instanceof ParameterizedType parameterized ? parameterized.getRawType() : type;

        return raw == Setting.class;
    }

    @Override
    public Setting<?> read(@NotNull Type type, @NotNull ConfigValue source) {
        if (source.isMap() || source.isList()) {
            throw new ConfigException(source, "Expected a setting id");
        }

        String raw = String.valueOf(source.unwrap());
        Setting<?> setting = SettingRegistry.getSetting(raw);

        if (setting == null) {
            throw new ConfigException(source, "Unknown setting '" + raw + "'");
        }

        return setting;
    }

    @Override
    public void write(@NotNull Type type, @Nullable Setting<?> value, @NotNull ConfigValue target) {
        target.set(value == null ? null : RegistryUtil.serialize(value.key()));
    }
}
