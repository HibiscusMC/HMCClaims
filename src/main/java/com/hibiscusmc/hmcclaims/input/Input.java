package com.hibiscusmc.hmcclaims.input;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
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
     * Submits this input with the raw response.
     * <p>
     * Parsing may need to leave the main thread (a player lookup, for instance), so the
     * outcome is reported through a future. Implementations still invoke the submit
     * callback on the main thread.
     *
     * @param raw The raw response sent by the player
     * @return A future completing with {@code null} if the input was parsed successfully,
     * otherwise with the reason it wasn't.
     */
    default CompletableFuture<@Nullable InputErrorReason> submit(String raw) {
        throw new IllegalArgumentException("Input submit[String] is not implemented yet");
    }

    /**
     * Cancels this input
     */
    default void cancel() {
        throw new IllegalArgumentException("Input cancel[] is not implemented yet");
    }
}