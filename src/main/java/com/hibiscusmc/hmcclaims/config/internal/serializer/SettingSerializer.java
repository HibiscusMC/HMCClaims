package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class SettingSerializer implements TypeSerializer<Setting> {

    public final static SettingSerializer INSTANCE = new SettingSerializer();

    private SettingSerializer() {
    }

    @Override
    public Setting deserialize(Type type, ConfigurationNode node) throws SerializationException {
        @Subst("setting_id")
        String rawSetting = node.getString();

        if (rawSetting == null) {
            return null;
        }

        Setting<?> setting = SettingRegistry.getSetting(rawSetting);

        if (setting == null) {
            throw new SerializationException("Could not find setting '" + rawSetting + "'");
        }

        return setting;
    }

    @Override
    public void serialize(Type type, @Nullable Setting setting, ConfigurationNode node) throws SerializationException {
        if (setting == null) {
            node.raw(null);
            throw new SerializationException("Setting is null");
        }

        Key key = setting.key();
        node.set(RegistryUtil.serialize(key));
    }
}