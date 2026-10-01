package com.hibiscusmc.hmcclaims.config;

import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;

import java.util.List;

/**
 * Keeps every loaded config file so they can be reloaded together.
 */
public class ConfigRegistry {

    private final List<ConfigHolder<?>> holders;

    public ConfigRegistry(@NotNull List<ConfigHolder<?>> holders) {
        this.holders = List.copyOf(holders);
    }

    public void reloadAll() {
        for (ConfigHolder<?> holder : holders) {
            holder.reload();
        }
    }
}
