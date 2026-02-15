package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.config.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.repository.UserRepository;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
    private UserManager manager;
    @Inject
    private StorageHolder holder;

    @Inject
    private Text text;

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

    @Command(names = "save")
    public void save(CommandSender sender, Player player) {
        Storage storage = holder.get();
        UserRepository repo = storage.users();

        User user = manager.getUser(player.getUniqueId())
                .orElse(null);

        if (user == null) {
            sender.sendMessage("User is not in cache!");
            return;
        }

        repo.saveUser(user)
                .thenAccept((v) -> sender.sendMessage("User saved to the database"))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    sender.sendMessage("Failed to save user");

                    return null;
                });
    }
}