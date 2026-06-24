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

    private static final String PLUGIN_NAMESPACE = "hmcclaims";
    private static final String DEFAULT_NAMESPACE = "minecraft";

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
     * @param namespace The namespace (e.g., "hmcjs").
     * @param value     The value.
     * @return A newly constructed {@link Key}.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static Key key(@NotNull @Subst("namespace") String namespace, @NotNull @Subst("id") String value) {
        return Key.key(namespace, value);
    }
}