package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.ConfigRegistry;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.selection.DisabledWorlds;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

import java.util.Set;

@Command(names = {"hmcclaims", "claimsadmin"}, permission = "hmcclaims.commands.admin")
public class HMCClaimsCommand implements CommandClass {

    @Inject
    private Set<Service> services;

    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private ConfigRegistry configs;

    @Inject
    private DisabledWorlds disabledWorlds;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private GuiRegistry guis;

    @Inject
    private FormRegistry forms;

    @Inject
    private UserManager users;

    @Inject
    private TextUtil text;

    @Command(names = "reload", permission = "hmcclaims.commands.admin.reload")
    public void reload(CommandSender sender) {
        for (Service service : services) {
            service.reload();
        }

        try {
            configs.reloadAll();
            disabledWorlds.clear();

            guis.reload();
            forms.reload();

            text.send(sender, messages.get().pluginReload());
        } catch (Exception e) {
            sender.sendRichMessage("<red>Plugin reload failed! See console for more information.");
            Logger.error("Plugin reload failed!", e);
        }
    }

    @Command(names = "bypass", permission = ClaimAccess.BYPASS_PERMISSION)
    public void bypass(@Sender Player sender) {
        User user = users.getUser(sender.getUniqueId()).orElse(null);

        if (user == null) {
            return;
        }

        boolean enabled = !user.bypassMode();

        user.bypassMode(enabled);

        text.send(sender, enabled ? messages.get().bypassEnabled() : messages.get().bypassDisabled());
    }
}