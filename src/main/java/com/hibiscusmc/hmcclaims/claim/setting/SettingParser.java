package com.hibiscusmc.hmcclaims.claim.setting;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for deserializing configuration settings from strings.
 * <p>
 * This can be implemented via lambda expressions or method references:
 * <pre>{@code
 * BaseSetting<Integer> intSetting = Integer::parseInt;
 * }</pre>
 *
 * @param <T> the target type of the deserialized setting
 */
@FunctionalInterface
public interface SettingParser {

    /**
     * Reconstitutes a setting value from its serialized string format.
     *
     * @param value the serialized string representation; must not be null
     * @return the deserialized object of type {@code T}
     * @throws IllegalArgumentException if the string format is invalid or cannot be parsed
     */
    <K> K deserialize(@NotNull String value);
}