package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettings {

    @Comment("Settings listed here still work but aren't shown in menus.")
    private List<String> hidden = new ArrayList<>();

    @Comment("The default value and the display of every claim setting. New settings are added automatically.")
    private Map<Setting<?>, Entry> settings = defaults();

    private static Map<Setting<?>, Entry> defaults() {
        Map<Setting<?>, Entry> defaults = new LinkedHashMap<>();

        for (Setting<?> setting : SettingRegistry.getAllSettings()) {
            defaults.put(setting, Entry.of(setting));
        }

        return defaults;
    }

    /**
     * Returns the settings that should be shown in menus, in registration order.
     */
    @NotNull
    public List<Setting<?>> visible() {
        return SettingRegistry.getAllSettings().stream()
                .filter(setting -> !isHidden(setting))
                .toList();
    }

    public boolean isHidden(@NotNull Setting<?> setting) {
        String id = RegistryUtil.serialize(setting.key());

        return hidden.stream().anyMatch(value -> value.equals(id) || value.equals(setting.key().asString()));
    }

    @NotNull
    public Entry entry(@NotNull Setting<?> setting) {
        Entry entry = settings.get(setting);

        return entry != null ? entry : Entry.of(setting);
    }

    /**
     * Returns the parsed default value of the setting, or {@code null} if it's empty or {@code null} in the file.
     */
    @Nullable
    public <T> T defaultValue(@NotNull Setting<T> setting) {
        Object raw = entry(setting).defaultValue();
        if (raw == null) {
            return null;
        }

        String value = String.valueOf(raw);
        if (value.isEmpty() || value.equals("null")) {
            return null;
        }

        T parsed = setting.parser().apply(value);

        return parsed == null || parsed.equals("null") ? null : parsed;
    }

    @Getter
    @Section
    @SuppressWarnings({"FieldMayBeFinal"})
    public static class Entry {

        private Object defaultValue;

        private String name = "";

        private List<String> description = new ArrayList<>();

        private ConfigItem icon = ConfigItem.of(Material.BOOK);

        public Entry() {
        }

        @NotNull
        public static Entry of(@NotNull Setting<?> setting) {
            Entry entry = new Entry();

            entry.defaultValue = setting.defaultValue();
            entry.name = setting.displayName();
            entry.description = Arrays.stream(("<gray>" + setting.description()).split("\n")).toList();
            entry.icon = ConfigItem.of(setting.icon());

            return entry;
        }
    }
}
