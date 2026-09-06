package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrolling list of buttons with an optional body — the Bedrock equivalent of a
 * paginated inventory GUI.
 */
public final class SimpleFormSpec implements FormSpec {

    private final String title;
    private final String content;
    private final List<FormButton> buttons;
    private final Runnable onClose;

    private SimpleFormSpec(String title, String content, List<FormButton> buttons, Runnable onClose) {
        this.title = title;
        this.content = content;
        this.buttons = buttons;
        this.onClose = onClose;
    }

    @Override
    public String title() {
        return title;
    }

    /**
     * @return The body text shown above the buttons. Never {@code null}, possibly empty.
     */
    @NotNull
    public String content() {
        return content;
    }

    /**
     * @return The buttons, in display order.
     */
    @NotNull
    public List<FormButton> buttons() {
        return buttons;
    }

    @Override
    public Runnable onClose() {
        return onClose;
    }

    /**
     * @return A new builder for a simple form.
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Incremental builder for {@link SimpleFormSpec}.
     */
    public static final class Builder {

        private String title = "";
        private String content = "";
        private final List<FormButton> buttons = new ArrayList<>();
        private Runnable onClose;

        private Builder() {
        }

        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        public Builder content(@NotNull String content) {
            this.content = content;
            return this;
        }

        public Builder button(@Nullable FormButton button) {
            if (button != null) {
                buttons.add(button);
            }

            return this;
        }

        public Builder button(@NotNull String text, @Nullable FormImage image, @NotNull Runnable action) {
            return button(new FormButton(text, image, action));
        }

        public Builder onClose(@Nullable Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        @NotNull
        public SimpleFormSpec build() {
            return new SimpleFormSpec(title, content, List.copyOf(buttons), onClose);
        }
    }
}
