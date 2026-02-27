package com.hibiscusmc.hmcclaims.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.audience.Audience;

import java.util.function.Consumer;

/**
 * An interface for working easily with the built-in Minecraft dialogs.
 */
@SuppressWarnings({"UnstableApiUsage"})
public interface Dialog {

    /**
     * Factory method to instantiate a specific dialog context with arguments.
     *
     * @param args Contextual data needed for the dialog (e.g., claim data, role data).
     * @return A new instance of the dialog.
     */
    default Dialog create(Object... args) {
        throw new IllegalArgumentException("Dialog create[Object[]] is not implemented yet");
    }

    /**
     * Factory method to instantiate a parameterless dialog context.
     *
     * @return A new instance of the dialog.
     */
    default Dialog create() {
        throw new IllegalArgumentException("Dialog create[] is not implemented yet");
    }

    /**
     * Renders the dialog and initiates the interaction for the target audience.
     *
     * @param audience The recipient of the dialog.
     */
    void show(Audience audience);

    /**
     * Registers a callback for when the user successfully submits their input.
     *
     * @param runnable The logic to execute upon submission.
     * @return This dialog instance for chaining.
     */
    default Dialog onSubmit(Runnable runnable) {
        throw new IllegalArgumentException("Dialog onSubmit[Runnable] is not implemented yet");
    }

    /**
     * Registers a callback that provides access to the user's response data.
     *
     * @param view A consumer providing the {@link DialogResponseView}.
     * @return This dialog instance for chaining.
     */
    default Dialog onSubmit(Consumer<DialogResponseView> view) {
        throw new IllegalArgumentException("Dialog onSubmit[Consumer<DialogResponseView>] is not implemented yet");
    }

    /**
     * Registers a callback for when the user exits the dialog without submitting.
     *
     * @param runnable The logic to execute upon cancellation.
     * @return This dialog instance for chaining.
     */
    default Dialog onCancel(Runnable runnable) {
        throw new IllegalArgumentException("Dialog onCancel[Runnable] is not implemented yet");
    }

    /**
     * Registers a callback for cancellation that provides access to the context.
     *
     * @param view A consumer providing the {@link DialogResponseView}.
     * @return This dialog instance for chaining.
     */
    default Dialog onCancel(Consumer<DialogResponseView> view) {
        throw new IllegalArgumentException("Dialog onCancel[Consumer<DialogResponseView>] is not implemented yet");
    }
}