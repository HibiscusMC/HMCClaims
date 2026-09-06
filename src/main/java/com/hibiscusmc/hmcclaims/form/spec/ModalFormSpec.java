package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A form with exactly two buttons and no icons — Bedrock's native confirmation dialog.
 */
public record ModalFormSpec(
        @NotNull String title,
        @NotNull String content,
        @NotNull String firstButton,
        @NotNull String secondButton,
        @NotNull Runnable onFirst,
        @NotNull Runnable onSecond,
        @Nullable Runnable onClose
) implements FormSpec {
}
