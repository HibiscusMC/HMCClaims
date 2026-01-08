package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.config.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.command.CommandSender;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.inject.Inject;

import java.util.Set;

@Command(names = "hmcclaims", permission = "hmcclaims.admin")
public class HMCClaimsCommand implements CommandClass {

    @Inject
    private Set<Service> services;

    @Inject
    private ConfigHolder<Settings> settings;
    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private Text text;

    @Command(names = "")
    public void test(CommandSender sender) {
        sender.sendRichMessage(settings.get().toString());
    }

    @Command(names = "reload", permission = "hmcclaims.admin.reload")
    public void reload(CommandSender sender) {
        for (Service service : services) {
            service.reload();
        }

        try {
            ConfigFactory.reload(Settings.class);
            ConfigFactory.reload(Messages.class);

            text.send(sender, messages.get().pluginReload());
        } catch (Exception e) {
            sender.sendRichMessage("<red>Plugin reload failed! See console for more information.");
            e.printStackTrace();
        }
    }
}