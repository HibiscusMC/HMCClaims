package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigSerializable
public class DefaultSettings {

    @Setting("default-settings")
    private Map<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>, String> defaultSettings = buildSettings();

    private Map<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>, String> buildSettings() {
        Map<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>, String> settings = new HashMap<>();

        for (com.hibiscusmc.hmcclaims.claim.setting.Setting<?> setting : SettingRegistry.getAllSettings()) {
            settings.put(
                    setting,
                    setting.defaultValue().toString()
            );
        }

        return settings;
    }
}