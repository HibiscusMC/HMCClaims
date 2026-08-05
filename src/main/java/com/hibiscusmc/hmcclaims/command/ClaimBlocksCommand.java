package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.command.argument.PlayerOrOffline;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.OptArg;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.Objects;

@Command(names = {"claimblocks", "cbs"}, permission = "hmcclaims.commands.claimblocks")
public class ClaimBlocksCommand implements CommandClass {

    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private PlaceholderUtil placeholder;
    @Inject
    private TextUtil text;

    @Command(names = {""})
    @Usage("[player]")
    public void base(CommandSender sender, @OptArg Player target) {
        Messages messages = messagesHolder.get();

        if (target == null && !(sender instanceof Player)) {
            text.send(sender, messages.commands().missingPlayer());
            return;
        }

        Player player = target != null && sender.hasPermission("hmcclaims.commands.claimblocks.other")
                ? target
                : (Player) sender;

        User user = userManager.getUser(player.getUniqueId()).orElse(null);
        if (user == null) {
            text.send(sender, messages.commands().missingPlayer());
            return;
        }

        text.send(sender, messages.commands().claimBlocks().summary(), MapUtil.add(
                placeholder.claimBlocks(user), "player_name", player.getName()
        ));
    }

    @Command(names = {"set"}, permission = "hmcclaims.commands.claimblocks.set")
    @Usage("<player> <amount>")
    public void set(CommandSender sender, @PlayerOrOffline OfflinePlayer target, int amount) {
        Messages messages = messagesHolder.get();

        if (target == null || !target.hasPlayedBefore()) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        userManager.getOrLoadUser(target.getUniqueId(), Objects.requireNonNull(target.getName(), "target name cannot be null"))
                .thenAccept(user -> {
                    if (user == null) {
                        text.send(sender, messages.commands().playerNotFound());
                        return;
                    }

                    user.claimBlocks(amount);
                    storageHolder.get().users()
                            .saveUser(user);

                    text.send(sender, messages.commands().claimBlocks().setAmount(), Map.of(
                            "amount", amount + "",
                            "name", target.getName() == null ? user.lastKnownName() : target.getName()
                    ));
                });
    }

    @Command(names = {"add"}, permission = "hmcclaims.commands.claimblocks.add")
    @Usage("<player> <amount>")
    public void add(CommandSender sender, @PlayerOrOffline OfflinePlayer target, int amount) {
        Messages messages = messagesHolder.get();

        if (target == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        userManager.getOrLoadUser(target.getUniqueId(), Objects.requireNonNull(target.getName(), "target name cannot be null"))
                .thenAccept(user -> {
                    if (user == null) {
                        text.send(sender, messages.commands().playerNotFound());
                        return;
                    }

                    long current = user.claimBlocks();

                    if (current + amount < 0) {
                        text.send(sender, messages.commands().claimBlocks().invalid());
                        return;
                    }

                    user.claimBlocks(current + amount);
                    storageHolder.get().users()
                            .saveUser(user);

                    text.send(sender, messages.commands().claimBlocks().setAmount(), Map.of(
                            "amount", amount + "",
                            "new_amount", user.claimBlocks() + "",
                            "name", target.getName() == null ? user.lastKnownName() : target.getName()
                    ));
                });
    }

    @Command(names = {"remove"}, permission = "hmcclaims.commands.claimblocks.remove")
    @Usage("<player> <amount>")
    public void remove(CommandSender sender, @PlayerOrOffline OfflinePlayer target, int amount) {
        Messages messages = messagesHolder.get();

        if (target == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        userManager.getOrLoadUser(target.getUniqueId(), Objects.requireNonNull(target.getName(), "target name cannot be null"))
                .thenAccept(user -> {
                    if (user == null) {
                        text.send(sender, messages.commands().playerNotFound());
                        return;
                    }

                    long current = user.claimBlocks();

                    if (current - amount < 0) {
                        text.send(sender, messages.commands().claimBlocks().invalid());
                        return;
                    }

                    user.claimBlocks(current - amount);
                    storageHolder.get().users()
                            .saveUser(user);

                    text.send(sender, messages.commands().claimBlocks().removeAmount(), Map.of(
                            "amount", amount + "",
                            "new_amount", user.claimBlocks() + "",
                            "name", target.getName() == null ? user.lastKnownName() : target.getName()
                    ));
                });
    }
}