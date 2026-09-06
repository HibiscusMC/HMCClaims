package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A form made of input rows submitted together. The Bedrock equivalent of a screen
 * full of toggles.
 */
public final class CustomFormSpec implements FormSpec {

    private final String title;
    private final List<FormComponent> components;
    private final Consumer<FormValues> onSubmit;
    private final Runnable onClose;

    private CustomFormSpec(String title, List<FormComponent> components, Consumer<FormValues> onSubmit, Runnable onClose) {
        this.title = title;
        this.components = components;
        this.onSubmit = onSubmit;
        this.onClose = onClose;
    }

    @Override
    public String title() {
        return title;
    }

    /**
     * @return The rows of the form, in display order.
     */
    @NotNull
    public List<FormComponent> components() {
        return components;
    }

    /**
     * @return The handler invoked on the main thread when the player submits the form.
     */
    @NotNull
    public Consumer<FormValues> onSubmit() {
        return onSubmit;
    }

    @Override
    public Runnable onClose() {
        return onClose;
    }

    /**
     * @return A new builder for a custom form.
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Incremental builder for {@link CustomFormSpec}.
     */
    public static final class Builder {

        private String title = "";
        private final List<FormComponent> components = new ArrayList<>();
        private Consumer<FormValues> onSubmit = values -> {
        };
        private Runnable onClose;

        private Builder() {
        }

        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        public Builder component(@Nullable FormComponent component) {
            if (component != null) {
                components.add(component);
            }

            return this;
        }

        public Builder label(@NotNull String text) {
            return component(new FormComponent.Label(text));
        }

        public Builder input(@NotNull String key, @NotNull String label, @NotNull String placeholder, @NotNull String defaultValue) {
            return component(new FormComponent.Input(key, label, placeholder, defaultValue));
        }

        public Builder toggle(@NotNull String key, @NotNull String label, boolean defaultValue) {
            return component(new FormComponent.Toggle(key, label, defaultValue));
        }

        public Builder dropdown(@NotNull String key, @NotNull String label, @NotNull List<String> options, int defaultIndex) {
            return component(new FormComponent.Dropdown(key, label, options, defaultIndex));
        }

        public Builder onSubmit(@NotNull Consumer<FormValues> onSubmit) {
            this.onSubmit = onSubmit;
            return this;
        }

        public Builder onClose(@Nullable Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        @NotNull
        public CustomFormSpec build() {
            return new CustomFormSpec(title, List.copyOf(components), onSubmit, onClose);
        }
    }
}