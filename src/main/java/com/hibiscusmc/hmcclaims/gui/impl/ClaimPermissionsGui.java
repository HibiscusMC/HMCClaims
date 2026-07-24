package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.window.Window;

public class ClaimPermissionsGui extends ClaimListGui {
    @Override
    public void loadConfig() {

    }

    @Override
    public void open(@NotNull Player player, GuiMetadata metadata) {
        Window.builder()
                .setTitle("Claim Permissions")
                .setFallbackWindow(metadata.previousPage())
                .open(player);
    }
}