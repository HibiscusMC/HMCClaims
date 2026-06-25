package com.hibiscusmc.hmcclaims.storage;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.util.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Singleton;

/**
 * A thread-safe wrapper and lifecycle manager for the plugin's data storage implementation.
 */
@Singleton
public class StorageHolder {

    @MonotonicNonNull
    private Storage implementation;

    /**
     * Gets the currently active storage implementation.
     *
     * @return The {@link Storage} instance, or {@code null} if not yet set.
     * @throws IllegalStateException if the storage has not been initialized
     */
    @NotNull
    @Contract(pure = true)
    public Storage get() {
        if (implementation == null) {
            throw new IllegalStateException("Storage has not been initialized yet.");
        }

        return implementation;
    }

    /**
     * Updates the storage implementation.
     * <p>
     * This should be called prior to {@link #initialize(Settings.Storage)}.
     *
     * @param implementation The new storage implementation to use.
     */
    public void update(@NotNull Storage implementation) {
        this.implementation = implementation;
    }

    /**
     * Connects to the storage and performs initial setup (e.g., table creation).
     *
     * @param config The storage-specific configuration settings.
     * @throws IllegalStateException if {@link #update(Storage)} has not been called.
     * @throws RuntimeException      if the storage fails to initialize.
     */
    public void initialize(Settings.Storage config) {
        if (implementation == null) {
            throw new IllegalStateException("Storage has not been initialized");
        }

        try {
            implementation.initialize(config);
        } catch (Exception ex) {
            Logger.error("Couldn't initialize " + implementation.name() + " storage. Shutting down...");
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gracefully shuts down the active storage implementation.
     *
     * @throws IllegalStateException if no storage is currently set.
     * @throws RuntimeException      if an error occurs during shutdown.
     */
    public void close() {
        if (implementation == null) {
            throw new IllegalStateException("Storage has not been initialized");
        }

        try {
            implementation.close();
        } catch (Exception ex) {
            Logger.error("Couldn't close " + implementation.name() + " storage");
            throw new RuntimeException(ex);
        }
    }
}