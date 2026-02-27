package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles asynchronous persistence and retrieval of {@link User} data.
 */
public interface UserRepository {

    /**
     * Retrieves a user from storage by their unique identifier.
     *
     * @param uuid The UUID of the player to retrieve.
     * @return A future containing the {@link User}, or a future completing
     * with {@code null} if no data exists for this UUID.
     */
    @NotNull
    CompletableFuture<User> getUser(@NotNull UUID uuid);

    /**
     * Retrieves a user from storage by their last known username.
     * <p>
     * <strong>Note:</strong> This lookup is often used for administrative commands and may
     * return older data if the player has changed their name recently.
     *
     * @param name The username to search for.
     * @return A future containing the {@link User}, or a future completing with {@code null}.
     */
    @NotNull
    CompletableFuture<User> getUserByName(@NotNull String name);

    /**
     * Persists a single user's data to storage.
     *
     * @param user The user object to save.
     * @return A future that completes when the save operation is finished.
     */
    @NotNull
    CompletableFuture<Void> saveUser(@NotNull User user);

    /**
     * Persists multiple users to storage.
     *
     * @param users The list of users to save.
     * @return A future that completes when all users have been processed.
     */
    @NotNull
    CompletableFuture<Void> saveUsers(@NotNull List<User> users);
}