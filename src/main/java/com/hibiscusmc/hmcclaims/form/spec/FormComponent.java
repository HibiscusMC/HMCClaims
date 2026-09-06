package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * One row of a {@link CustomFormSpec}
 */
public sealed interface FormComponent {

    /**
     * @return The key this component's value is stored under, or {@code null} for labels.
     */
    String key();

    /**
     * A read-only line of text.
     *
     * @param text The text to display.
     */
    record Label(@NotNull String text) implements FormComponent {

        @Override
        public String key() {
            return null;
        }
    }

    /**
     * A free-text field. Submits a {@link String}.
     *
     * @param key          The lookup key.
     * @param label        The text shown above the field.
     * @param placeholder  The greyed-out hint shown while the field is empty.
     * @param defaultValue The value the field starts with.
     */
    record Input(
            @NotNull String key, @NotNull String label,
            @NotNull String placeholder, @NotNull String defaultValue
    ) implements FormComponent {
    }

    /**
     * An on/off switch. Submits a {@link Boolean}.
     *
     * @param key          The lookup key.
     * @param label        The text shown next to the switch.
     * @param defaultValue The state the switch starts in.
     */
    record Toggle(@NotNull String key, @NotNull String label, boolean defaultValue) implements FormComponent {
    }

    /**
     * A list of mutually exclusive options. Submits the selected {@link Integer} index.
     *
     * @param key          The lookup key.
     * @param label        The text shown above the dropdown.
     * @param options      The selectable options, in order.
     * @param defaultIndex The index selected when the form opens.
     */
    record Dropdown(
            @NotNull String key, @NotNull String label,
            @NotNull List<String> options, int defaultIndex
    ) implements FormComponent {
    }

    /**
     * A numeric slider. Submits a {@link Float}.
     *
     * @param key          The lookup key.
     * @param label        The text shown above the slider.
     * @param min          The lowest selectable value.
     * @param max          The highest selectable value.
     * @param step         The increment between selectable values.
     * @param defaultValue The value the slider starts at.
     */
    record Slider(
            @NotNull String key, @NotNull String label,
            float min, float max, float step, float defaultValue
    ) implements FormComponent {
    }

    /**
     * A slider that snaps between named steps. Submits the selected {@link Integer} index.
     *
     * @param key          The lookup key.
     * @param label        The text shown above the slider.
     * @param steps        The named steps, in order.
     * @param defaultIndex The index selected when the form opens.
     */
    record StepSlider(
            @NotNull String key, @NotNull String label,
            @NotNull List<String> steps, int defaultIndex
    ) implements FormComponent {
    }
}