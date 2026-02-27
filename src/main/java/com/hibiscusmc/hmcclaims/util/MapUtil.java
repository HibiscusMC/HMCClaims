package com.hibiscusmc.hmcclaims.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A versatile utility for {@link Map} manipulation, providing both a fluent builder
 * pattern and static helper methods.
 */
public class MapUtil<K, V> {

    private final Map<K, V> map;

    /**
     * Constructs a new MapUtil instance wrapping the provided map.
     * <p>
     * <b>Warning:</b> This constructor does not create a copy. Operations
     * performed via {@link #add(K, V)} will directly modify the input map.
     *
     * @param map The map to wrap and modify.
     */
    public MapUtil(Map<K, V> map) {
        this.map = map;
    }

    /**
     * Adds a key-value pair to the underlying map.
     *
     * @param key   The key to insert.
     * @param value The value to associate with the key.
     * @return This {@link MapUtil} instance for method chaining.
     */
    @NotNull
    @Contract("_, _ -> this")
    public MapUtil<K, V> add(@NotNull K key, @NotNull V value) {
        map.put(key, value);

        return this;
    }

    /**
     * Returns the underlying map containing all added entries.
     *
     * @return The {@link Map} instance managed by this utility.
     */
    @NotNull
    @Contract(pure = true)
    public Map<K, V> build() {
        return map;
    }

    /**
     * Creates a new MapUtil instance starting with a defensive copy of the provided map.
     * <p>
     * Use this method when you want to build upon an existing map without
     * modifying the original data source.
     *
     * @param map The source map to copy.
     * @return A new {@link MapUtil} instance wrapping a mutable {@link HashMap} copy.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <K, V> MapUtil<K, V> chain(@NotNull Map<K, V> map) {
        return new MapUtil<>(new HashMap<>(map));
    }

    /**
     * Injects a new key-value pair into an existing map.
     * <p>
     * <b>Note:</b> The provided map must be mutable (e.g., a {@link HashMap}),
     * otherwise an {@link UnsupportedOperationException} will be thrown.
     *
     * @param map   The mutable map to modify.
     * @param key   The key to insert.
     * @param value The value to associate with the key.
     * @return The same map instance (param1) with the new entry added.
     * @throws IllegalArgumentException if the provided map is immutable.
     */
    @NotNull
    @Contract("_, _, _ -> param1")
    public static <K, V> Map<K, V> add(@NotNull Map<K, V> map, @NotNull K key, @NotNull V value) {
        try {
            map.put(key, value);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("MapUtil#add() requires a mutable map implementation", e);
        }

        return map;
    }

    /**
     * Merges two maps into a new third map.
     *
     * @param a The first map.
     * @param b The second map.
     * @return A new {@link HashMap} containing the union of both maps.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <K, V> Map<K, V> concat(@NotNull Map<K, V> a, @NotNull Map<K, V> b) {
        Map<K, V> c = new HashMap<>();
        c.putAll(a);
        c.putAll(b);

        return c;
    }

    /**
     * Merges multiple maps into a single new {@link HashMap}.
     *
     * @param maps A variable number of maps to merge.
     * @param <K>  The type of keys.
     * @param <V>  The type of values.
     * @return A new {@link HashMap} containing all entries from the provided maps.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    @SafeVarargs
    public static <K, V> Map<K, V> concat(@NotNull Map<K, V>... maps) {
        Map<K, V> combined = new HashMap<>();

        for (Map<K, V> map : maps) {
            combined.putAll(map);
        }

        return combined;
    }
}