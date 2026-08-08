package com.hibiscusmc.hmcclaims.hook.papi;

import com.hibiscusmc.hmcclaims.hook.Hook;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

public class PlaceholderAPIHook implements Hook {

    @Inject
    private Injector injector;

    @Override
    public @NotNull String dependsOn() {
        return "PlaceholderAPI";
    }

    @Override
    public @NotNull LoadStrategy loadStrategy() {
        return LoadStrategy.PLUGIN_ENABLE;
    }

    @Override
    public void register() {
        PAPIExpansion expansion = injector.getInstance(PAPIExpansion.class);

        expansion.register();
    }

    @Override
    public void unregister() {
        PAPIExpansion expansion = PAPIExpansion.instance();

        if (expansion != null) {
            expansion.unregister();
        }
    }
}