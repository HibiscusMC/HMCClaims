package com.hibiscusmc.hmcclaims.input;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An interface for working easily with chat-based player inputs.
 */
public interface Input<T> {

    /**
     * Registers a callback that provides access to the user's response data.
     *
     * @param view A consumer providing the parsed object.
     * @return This input instance for chaining.
     */
    default Input<T> onSubmit(Consumer<T> view) {
        throw new IllegalArgumentException("Input onSubmit[Consumer<T>] is not implemented yet");
    }

    /**
     * Registers a callback for when the user exits the input without submitting.
     *
     * @param runnable The logic to execute upon cancellation.
     * @return This input instance for chaining.
     */
    default Input<T> onCancel(Runnable runnable) {
        throw new IllegalArgumentException("Input onCancel[Runnable] is not implemented yet");
    }

    /**
     * Submits this input with the raw response
     *
     * @param raw The raw response sent by the player
     * @return {@code true} if the input was parsed successfully.
     */
    default @Nullable InputErrorReason submit(String raw) {
        throw new IllegalArgumentException("Input submit[String] is not implemented yet");
    }

    /**
     * Cancels this input
     */
    default void cancel() {
        throw new IllegalArgumentException("Input cancel[] is not implemented yet");
    }
}