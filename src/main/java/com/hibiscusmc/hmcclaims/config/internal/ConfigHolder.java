package com.hibiscusmc.hmcclaims.config.internal;

import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * A generic wrapper that facilitates live-reloading of configuration data.
 */
@NoArgsConstructor
public class ConfigHolder<T> {

    private T instance;
    private Path path;

    /**
     * Updates the file system path associated with this configuration.
     *
     * @param path The {@link Path} to the .yml file.
     */
    public void path(Path path) {
        this.path = path;
    }

    /**
     * @return The current {@link Path} where the config is stored on disk.
     */
    public Path path() {
        return path;
    }

    /**
     * Swaps the internal configuration instance with a newly loaded one.
     *
     * @param newInstance The fresh configuration object.
     */
    public void update(T newInstance) {
        this.instance = newInstance;
    }

    /**
     * Retrieves the current configuration instance.
     *
     * @return The active configuration POJO.
     */
    public T get() {
        return instance;
    }
}