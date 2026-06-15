package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.gui.SubClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class SubClaimManageGui extends ClaimManageGui {

    @Inject
    private ConfigHolder<SubClaimManageConfig> configHolder;

    private GuiTemplate.SimpleIcon inheritPermissionsIcon;
    private GuiTemplate.SimpleIcon inheritPermissionsSuccessIcon;

    @Override
    public void loadConfig() {
        SubClaimManageConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        loadConfig(config);

        inheritPermissionsIcon = config.inheritPermissionsIcon();
        inheritPermissionsSuccessIcon = config.inheritPermissionsSucesssIcon();
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

        Gui gui = Gui.gui()
                .title(TextUtil.parse(title, Map.of(
                        "claim_name", claim.name()
                )))
                .rows(rows)
                .disableAllInteractions()
                .create();

        scheduler.scheduleAsync(() -> {
            buildSubIcons(player, gui, claim);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildSubIcons(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim) {
        buildIcons(player, gui, claim);

        gui.setItem(inheritPermissionsIcon.slot(), new GuiItem(inheritPermissionsIcon.item(), action -> {
            System.out.println("perms inherited");
            claim.inheritPermissions();

            gui.updateItem(inheritPermissionsSuccessIcon.slot(), new GuiItem(inheritPermissionsSuccessIcon.item()));
        }));
    }
}