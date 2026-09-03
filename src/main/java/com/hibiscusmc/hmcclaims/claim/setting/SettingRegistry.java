package com.hibiscusmc.hmcclaims.claim.setting;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A centralized registry for all valid claim settings.
 */
public class SettingRegistry {

    /**
     * Internal map storing settings indexed by their unique key string.
     */
    private final static Map<String, Setting<?>> SETTINGS = new HashMap<>();

    /**
     * Private constructor to prevent instantiation of a utility-based registry.
     */
    private SettingRegistry() {
    }

    static {
        register(Setting.JOIN_MESSAGE);
        register(Setting.LEAVE_MESSAGE);

        register(Setting.MOB_EXPLOSIONS);
        register(Setting.BLOCK_EXPLOSIONS);

        register(Setting.PVP);
    }

    /**
     * Registers a new setting into the system.
     *
     * @param setting The setting instance to register.
     * @throws IllegalArgumentException If a setting with the same key is already registered.
     */
    public static void register(Setting<?> setting) {
        String key = setting.key().asString();

        if (SETTINGS.containsKey(key)) {
            throw new IllegalArgumentException("Setting already registered: " + setting.key().asString());
        }

        SETTINGS.put(key, setting);
    }

    /**
     * Retrieves a setting by its string key.
     *
     * @param key The key string (e.g., "hmcclaims:mob_explosions").
     * @return The {@link Setting} instance, or {@code null} if not found.
     */
    @Nullable
    @Contract(pure = true)
    public static <T> Setting<T> getSetting(String key) {
        return getSetting(RegistryUtil.key(key));
    }

    /**
     * Retrieves a setting by its namespaced Key.
     *
     * @param key The {@link Key} to look up.
     * @return The {@link Setting} instance, or {@code null} if not found.
     * @noinspection unchecked
     */
    @Nullable
    @Contract(pure = true)
    public static <T> Setting<T> getSetting(Key key) {
        return (Setting<T>) SETTINGS.get(key.asString());
    }

    /**
     * Gets a read-only collection of all currently registered settings.
     *
     * @return An unmodifiable view of the registered settings.
     */
    @NotNull
    @Contract(pure = true)
    public static Collection<Setting<?>> getAllSettings() {
        return Collections.unmodifiableCollection(SETTINGS.values());
    }
}