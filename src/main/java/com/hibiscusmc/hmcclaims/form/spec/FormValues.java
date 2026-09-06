package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * The values a player submitted through a {@link CustomFormSpec}, keyed by the
 * {@link FormComponent#key()} of the component that produced them.
 *
 * @param values The raw submitted values.
 */
public record FormValues(@NotNull Map<String, Object> values) {

    /**
     * Reads a {@link FormComponent.Toggle} value.
     *
     * @param key          The component key.
     * @param defaultValue Returned when the key is missing or holds another type.
     * @return The submitted state.
     */
    public boolean toggle(@NotNull String key, boolean defaultValue) {
        return values.get(key) instanceof Boolean value ? value : defaultValue;
    }

    /**
     * Reads a {@link FormComponent.Input} value.
     *
     * @param key The component key.
     * @return The submitted text, or {@code null} when the key is missing.
     */
    @Nullable
    public String input(@NotNull String key) {
        return values.get(key) instanceof String value ? value : null;
    }

    /**
     * Reads the selected index of a {@link FormComponent.Dropdown} or
     * {@link FormComponent.StepSlider}.
     *
     * @param key          The component key.
     * @param defaultValue Returned when the key is missing or holds another type.
     * @return The selected index.
     */
    public int index(@NotNull String key, int defaultValue) {
        return values.get(key) instanceof Number value ? value.intValue() : defaultValue;
    }

    /**
     * Reads a {@link FormComponent.Slider} value.
     *
     * @param key          The component key.
     * @param defaultValue Returned when the key is missing or holds another type.
     * @return The submitted number.
     */
    public float number(@NotNull String key, float defaultValue) {
        return values.get(key) instanceof Number value ? value.floatValue() : defaultValue;
    }

    /**
     * @param key The component key.
     * @return {@code true} if the form submitted a value for this key.
     */
    public boolean has(@NotNull String key) {
        return values.containsKey(key);
    }
}
