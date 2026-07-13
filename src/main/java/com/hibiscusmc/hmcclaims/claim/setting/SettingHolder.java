package com.hibiscusmc.hmcclaims.claim.setting;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * A generic container class that associates a configuration {@link Setting} with its
 * corresponding value.
 * <p>This class acts as a mutable wrapper around an immutable setting definition,
 * allowing configuration values to be updated dynamically.</p>
 *
 * @param <T> The data type of the setting's value (e.g., String, Integer, Boolean).
 */
@Getter
public class SettingHolder<T> {
    private final Setting<T> setting;

    @Setter
    @Nullable
    private T value;

    /**
     * Private constructor to enforce initialization through the static factory method.
     *
     * @param setting The setting definition to wrap.
     */
    private SettingHolder(Setting<T> setting) {
        this.setting = setting;
        this.value = setting.defaultValue();
    }

    /**
     * Factory method to create a new {@code SettingHolder} instance for a given setting.
     * The initial value will remain {@code null} until explicitly set using {@link #value(K)}.
     *
     * @param <K>     The data type of the setting value.
     * @param setting The {@link Setting} definition to be held.
     * @return A new {@code SettingHolder} instance wrapping the specified setting.
     */
    public static <K> SettingHolder<K> from(Setting<K> setting) {
        return new SettingHolder<>(setting);
    }
}