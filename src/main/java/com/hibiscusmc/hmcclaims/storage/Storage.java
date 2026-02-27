package com.hibiscusmc.hmcclaims.storage;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.storage.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the interface for plugin data persistence.
 */
public interface Storage {

    /**
     * Returns the display name of the storage implementation.
     *
     * @return A string identifying the storage type (e.g., "PostgreSQL", "MariaDB").
     */
    @NotNull
    String name();

    /**
     * Performs initial connection setup and prepares data structures.
     *
     * @param storage The configuration settings for this storage backend.
     * @throws Exception if connection or setup fails.
     */
    void initialize(@NotNull Settings.Storage storage) throws Exception;

    /**
     * Gracefully terminates connections and flushes any pending data.
     */
    void close();

    /**
     * Provides access to the user data management layer.
     *
     * @return An implementation of {@link UserRepository} tied to this storage backend.
     */
    @NotNull
    UserRepository users();

    /**
     * Provides access to the claim data management layer.
     *
     * @return An implementation of {@link ClaimRepository} tied to this storage backend.
     */
    @NotNull
    ClaimRepository claims();
}