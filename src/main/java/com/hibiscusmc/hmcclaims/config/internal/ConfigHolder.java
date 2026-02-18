package com.hibiscusmc.hmcclaims.config.internal;

import java.nio.file.Path;

public class ConfigHolder<T> {

    private T instance;
    private Path path;

    public void path(Path path) {
        this.path = path;
    }

    public Path path() {
        return path;
    }

    public void update(T newInstance) {
        this.instance = newInstance;
    }

    public T get() {
        return instance;
    }
}