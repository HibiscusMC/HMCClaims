package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single button of a {@link SimpleFormSpec}.
 *
 * @param text   The label, already converted to Bedrock formatting. May contain {@code \n}.
 * @param image  The optional icon shown to the left of the label.
 * @param action Executed on the main thread when the player taps the button.
 */
public record FormButton(@NotNull String text, @Nullable FormImage image, @NotNull Runnable action) {
}
