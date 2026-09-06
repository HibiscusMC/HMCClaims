package com.hibiscusmc.hmcclaims.hook.floodgate;

import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.hook.Hook;
import com.hibiscusmc.hmcclaims.util.Logger;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

/**
 * Activates Bedrock form support once Floodgate is confirmed to be installed.
 * <p>
 * Nothing outside {@link com.hibiscusmc.hmcclaims.form.FloodgateBridge} touches Geyser
 * classes, and that bridge is only constructed from here, so a server without Floodgate
 * never has to resolve them.
 */
public class FloodgateHook implements Hook {

    @Inject
    private FormService forms;

    @Override
    public @NotNull String dependsOn() {
        return "floodgate";
    }

    @Override
    public @NotNull LoadStrategy loadStrategy() {
        return LoadStrategy.PLUGIN_ENABLE;
    }

    @Override
    public void register() {
        forms.enable();

        Logger.log("Floodgate detected, Bedrock forms are active.");
    }

    @Override
    public void unregister() {
        forms.disable();
    }
}
