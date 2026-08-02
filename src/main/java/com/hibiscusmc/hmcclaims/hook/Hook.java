package com.hibiscusmc.hmcclaims.hook;

import org.jetbrains.annotations.NotNull;

public interface Hook {

    @NotNull String dependsOn();

    @NotNull LoadStrategy loadStrategy();

    default void registerLoader() {
        throw new UnsupportedOperationException("This hook doesn't provide a custom loader.");
    }

    void register();

    void unregister();

    enum LoadStrategy {
        PLUGIN_ENABLE,
        PROVIDED
    }
}