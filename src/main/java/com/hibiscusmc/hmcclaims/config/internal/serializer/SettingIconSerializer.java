package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.config.gui.ClaimSettingsConfig;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class SettingIconSerializer implements TypeSerializer<ClaimSettingsConfig.SettingIcon.BaseSettingIcon> {

    public final static SettingIconSerializer INSTANCE = new SettingIconSerializer();

    private SettingIconSerializer() {
    }

    @Override
    public ClaimSettingsConfig.SettingIcon.BaseSettingIcon deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.node("input").virtual()) {
            return node.get(ClaimSettingsConfig.SettingIcon.BooleanSettingIcon.class);
        } else {
            return node.get(ClaimSettingsConfig.SettingIcon.InputSettingIcon.class);
        }
    }

    @Override
    public void serialize(Type type, ClaimSettingsConfig.SettingIcon.BaseSettingIcon obj, ConfigurationNode node) throws SerializationException {
        if (obj instanceof ClaimSettingsConfig.SettingIcon.BooleanSettingIcon) {
            node.set(ClaimSettingsConfig.SettingIcon.BooleanSettingIcon.class, (ClaimSettingsConfig.SettingIcon.BooleanSettingIcon) obj);
        } else if (obj instanceof ClaimSettingsConfig.SettingIcon.InputSettingIcon) {
            node.set(ClaimSettingsConfig.SettingIcon.InputSettingIcon.class, (ClaimSettingsConfig.SettingIcon.InputSettingIcon) obj);
        }
    }
}