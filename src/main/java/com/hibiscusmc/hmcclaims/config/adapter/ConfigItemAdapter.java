package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigItemAdapter implements Adapter<ConfigItem> {

    private final static String MATERIAL = "material";
    private final static String NAME = "name";
    private final static String LORE = "lore";
    private final static String ITEM_FLAGS = "item-flags";
    private final static String COMPONENTS = "components";

    @Override
    public ConfigItem read(@NotNull Type type, @NotNull ConfigValue source) {
        if (source.isList()) {
            throw new ConfigException(source, "Expected an item id or a map");
        }

        if (!source.isMap()) {
            return new ConfigItem(id(source, source), null, null, List.of(), Map.of(), source.path());
        }

        ConfigValue material = source.at(MATERIAL);
        if (material.isNull()) {
            throw new ConfigException(source, "Missing material for item");
        }

        ConfigValue name = source.at(NAME);
        ConfigValue lore = source.at(LORE);
        ConfigValue flags = source.at(ITEM_FLAGS);

        return new ConfigItem(
                id(material, source),
                name.isNull() ? null : name.asString(""),
                lore.isNull() ? null : lore.asList(String.class),
                flags.isNull() ? List.of() : flags.asList(String.class),
                components(source.at(COMPONENTS)),
                source.path()
        );
    }

    @Override
    public void write(@NotNull Type type, @Nullable ConfigItem value, @NotNull ConfigValue target) {
        if (value == null) {
            target.set(null);
            return;
        }

        if (value.simple()) {
            target.set(value.material());
            return;
        }

        target.set(Map.of());
        target.at(MATERIAL).set(value.material());

        if (value.name() != null) {
            target.at(NAME).set(value.name());
        }

        if (value.lore() != null) {
            target.at(LORE).set(value.lore());
        }

        if (!value.flags().isEmpty()) {
            target.at(ITEM_FLAGS).set(value.flags());
        }

        if (!value.components().isEmpty()) {
            target.at(COMPONENTS).set(value.components());
        }
    }

    private String id(ConfigValue value, ConfigValue owner) {
        if (value.isMap() || value.isList()) {
            throw new ConfigException(value, "Expected an item id");
        }

        String id = String.valueOf(value.unwrap()).trim();
        if (id.isEmpty()) {
            throw new ConfigException(owner, "Missing item id");
        }

        return id;
    }

    private Map<String, Object> components(ConfigValue value) {
        if (value.isNull()) {
            return Map.of();
        }

        if (!value.isMap()) {
            throw new ConfigException(value, "Expected a map of components");
        }

        Map<String, Object> components = new LinkedHashMap<>();

        for (Map.Entry<String, ConfigValue> entry : value.entries().entrySet()) {
            if (!ConfigItem.COMPONENTS.contains(entry.getKey())) {
                throw new ConfigException(entry.getValue(), "Unknown item component '" + entry.getKey() + "'");
            }

            components.put(entry.getKey(), entry.getValue().unwrap());
        }

        return components;
    }
}
