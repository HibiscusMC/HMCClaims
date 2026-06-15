package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.command.CommandSender;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.inject.Inject;

import java.util.Set;

@Command(names = {"hmcclaims", "claimsadmin"}, permission = "hmcclaims.commands.admin")
public class HMCClaimsCommand implements CommandClass {

    @Inject
    private Set<Service> services;

    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private GuiRegistry guis;

    @Inject
    private TextUtil text;

    @Command(names = "reload", permission = "hmcclaims.commands.admin.reload")
    public void reload(CommandSender sender) {
        for (Service service : services) {
            service.reload();
        }

        try {
            for (Class<?> clazz : ConfigFactory.all(false)) {
                ConfigFactory.reload(clazz);
            }

            guis.reload();

            text.send(sender, messages.get().pluginReload());
        } catch (Exception e) {
            sender.sendRichMessage("<red>Plugin reload failed! See console for more information.");
            e.printStackTrace();
        }
    }
}