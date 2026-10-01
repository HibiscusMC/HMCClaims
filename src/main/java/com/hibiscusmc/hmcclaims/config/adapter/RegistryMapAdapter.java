package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.util.Logger;
import net.kyori.adventure.key.InvalidKeyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Reads a {@code Map<K, V>} keyed by registry entries, such as permissions or settings.
 * <p>
 * Ids that are no longer registered are ignored but stay in the file. Registered entries missing
 * from the file get their default written into it, so new entries show up without any config change.
 */
public class RegistryMapAdapter<K, V> implements Adapter<Map<K, V>> {

    private final String label;

    private final Predicate<Type> keyMatcher;

    private final Function<String, K> lookup;

    private final Function<K, String> serializer;

    private final Supplier<Collection<K>> registered;

    private final Function<K, V> defaults;

    public RegistryMapAdapter(
            @NotNull String label, @NotNull Predicate<Type> keyMatcher, @NotNull Function<String, K> lookup,
            @NotNull Function<K, String> serializer, @NotNull Supplier<Collection<K>> registered,
            @NotNull Function<K, V> defaults
    ) {
        this.label = label;
        this.keyMatcher = keyMatcher;
        this.lookup = lookup;
        this.serializer = serializer;
        this.registered = registered;
        this.defaults = defaults;
    }

    /**
     * Whether the type is a {@code Map} whose keys are the registry entries of this adapter.
     */
    public boolean matches(@NotNull Type type) {
        return type instanceof ParameterizedType parameterized
                && parameterized.getRawType() == Map.class
                && parameterized.getActualTypeArguments().length == 2
                && keyMatcher.test(parameterized.getActualTypeArguments()[0]);
    }

    @Override
    public Map<K, V> read(@NotNull Type type, @NotNull ConfigValue source) {
        if (!source.isMap()) {
            throw new ConfigException(source, "Expected a map of " + label + " entries");
        }

        Type valueType = valueType(type);
        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<String, ConfigValue> entry : source.entries().entrySet()) {
            K key = find(entry.getKey());

            if (key == null) {
                Logger.warning("Ignoring unknown {} '{}' at '{}'", label, entry.getKey(), source.path());
                continue;
            }

            V value = entry.getValue().as(valueType);
            if (value != null) {
                result.put(key, value);
            }
        }

        for (K key : registered.get()) {
            if (result.containsKey(key)) {
                continue;
            }

            V value = defaults.apply(key);

            source.at(serializer.apply(key)).set(valueType, value);
            result.put(key, value);
        }

        return result;
    }

    @Override
    public void write(@NotNull Type type, @Nullable Map<K, V> value, @NotNull ConfigValue target) {
        if (value == null) {
            target.set(null);
            return;
        }

        Type valueType = valueType(type);

        target.set(Map.of());

        for (Map.Entry<K, V> entry : value.entrySet()) {
            target.at(serializer.apply(entry.getKey())).set(valueType, entry.getValue());
        }
    }

    @Nullable
    private K find(String id) {
        try {
            return lookup.apply(id);
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    private Type valueType(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[1];
    }
}
