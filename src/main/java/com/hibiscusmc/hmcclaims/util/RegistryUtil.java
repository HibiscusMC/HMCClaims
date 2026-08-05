package com.hibiscusmc.hmcclaims.util;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for creating and managing namespaced {@link Key} objects.
 * <p>
 * Centralizes the plugin's namespace to ensure consistency across
 * Registry lookups and Persistent Data Container (PDC) tags.
 */
public class RegistryUtil {

    private final static String PLUGIN_NAMESPACE = "hmcclaims";
    private final static String DEFAULT_NAMESPACE = "minecraft";

    /**
     * Creates a new {@link Key} using the minecraft namespace.
     *
     * @param value The key identifier (must be lowercase and follow [a-z0-9._-]).
     * @return A newly constructed {@link Key}.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Key minecraft(@NotNull @Subst("id") String value) {
        return Key.key(DEFAULT_NAMESPACE, value);
    }

    /**
     * Creates a new {@link Key} using either the specified namespace or the plugin's default namespace.
     *
     * @param value The key identifier (must be lowercase and follow [a-z0-9._-]).
     * @return A newly constructed {@link Key}.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Key key(@NotNull @Subst("id") String value) {
        if (value.contains(":")) {
            return Key.key(value);
        }

        return Key.key(PLUGIN_NAMESPACE, value);
    }

    /**
     * Creates a new {@link Key} with a custom namespace.
     *
     * @param namespace The namespace (e.g., "hmc").
     * @param value     The value.
     * @return A newly constructed {@link Key}.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static Key key(@NotNull @Subst("namespace") String namespace, @NotNull @Subst("id") String value) {
        return Key.key(namespace, value);
    }

    /**
     * Serializes a {@link Key} into a string. If the {@link Key} namespace
     * is equal to the plugin's namespace ({@link #PLUGIN_NAMESPACE}), it will only
     * return the value of the key.
     *
     * @param key the key to serialize
     * @return the serialized key with the format of {@code namespace:key} or {@code key}
     */
    @NotNull
    @Contract(pure = true)
    public static String serialize(@NotNull Key key) {
        if (key.namespace().equals(PLUGIN_NAMESPACE)) {
            return key.value();
        }

        return key.namespace() + ":" + key.value();
    }
}