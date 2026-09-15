package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.command.argument.PlayerName;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.economy.EconomyService;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.PlayerResolver;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.OptArg;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Command(names = {"claimblocks", "cbs"}, permission = "hmcclaims.commands.claimblocks")
public class ClaimBlocksCommand implements CommandClass {

    @Inject
    private UserManager userManager;

    @Inject
    private PlayerResolver playerResolver;

    @Inject
    private EconomyService economy;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ConfigHolder<Settings> settingsHolder;

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

    @Command(names = {"buy", "purchase"}, permission = "hmcclaims.commands.claimblocks.buy")
    @Usage("<amount>")
    public void buy(@Sender Player sender, int amount) {
        Messages messages = messagesHolder.get();
        Settings.ClaimBlocks.Purchase purchase = settingsHolder.get().claimBlocks().purchase();

        if (!purchase.enabled() || !economy.available()) {
            text.send(sender, messages.commands().claimBlocks().purchaseDisabled());
            return;
        }

        int min = Math.max(1, purchase.minAmount());
        boolean unlimited = purchase.maxAmount() < 0;

        if (amount < min || (!unlimited && amount > purchase.maxAmount())) {
            text.send(sender, messages.commands().claimBlocks().purchaseOutOfRange(), Map.of(
                    "min", min + "",
                    "max", unlimited ? "∞" : purchase.maxAmount() + ""
            ));
            return;
        }

        User user = userManager.getUser(sender.getUniqueId()).orElse(null);
        if (user == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        double cost = amount * purchase.price();
        double balance = economy.balance(sender);

        if (balance < cost) {
            text.send(sender, messages.commands().claimBlocks().notEnoughMoney(), Map.of(
                    "cost", economy.format(cost),
                    "balance", economy.format(balance)
            ));
            return;
        }

        if (!economy.withdraw(sender, cost)) {
            text.send(sender, messages.commands().claimBlocks().purchaseFailed());
            return;
        }

        user.claimBlocks(user.claimBlocks() + amount);
        storageHolder.get().users()
                .saveUser(user);

        text.send(sender, messages.commands().claimBlocks().purchased(), Map.of(
                "amount", amount + "",
                "cost", economy.format(cost),
                "new_amount", user.claimBlocks() + ""
        ));
    }

    @Command(names = {"set"}, permission = "hmcclaims.commands.claimblocks.set")
    @Usage("<player> <amount>")
    public void set(CommandSender sender, @PlayerName String playerName, int amount) {
        Messages messages = messagesHolder.get();

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        loadTarget(sender, playerName).thenAccept(user -> {
            if (user == null) {
                return;
            }

            user.claimBlocks(amount);
            storageHolder.get().users()
                    .saveUser(user);

            text.send(sender, messages.commands().claimBlocks().setAmount(), Map.of(
                    "amount", amount + "",
                    "name", user.lastKnownName()
            ));
        });
    }

    @Command(names = {"add"}, permission = "hmcclaims.commands.claimblocks.add")
    @Usage("<player> <amount>")
    public void add(CommandSender sender, @PlayerName String playerName, int amount) {
        Messages messages = messagesHolder.get();

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        loadTarget(sender, playerName).thenAccept(user -> {
            if (user == null) {
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

            text.send(sender, messages.commands().claimBlocks().addAmount(), Map.of(
                    "amount", amount + "",
                    "new_amount", user.claimBlocks() + "",
                    "name", user.lastKnownName()
            ));
        });
    }

    @Command(names = {"remove"}, permission = "hmcclaims.commands.claimblocks.remove")
    @Usage("<player> <amount>")
    public void remove(CommandSender sender, @PlayerName String playerName, int amount) {
        Messages messages = messagesHolder.get();

        if (amount < 0) {
            text.send(sender, messages.commands().claimBlocks().invalid());
            return;
        }

        loadTarget(sender, playerName).thenAccept(user -> {
            if (user == null) {
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
                    "name", user.lastKnownName()
            ));
        });
    }

    /**
     * Resolves a typed name and loads that player's data.
     *
     * @return A future completing with the user, or with {@code null} after telling the
     * sender the player doesn't exist.
     */
    @NotNull
    private CompletableFuture<@Nullable User> loadTarget(@NotNull CommandSender sender, @NotNull String playerName) {
        return playerResolver.resolve(playerName).thenCompose(target -> {
            if (target == null) {
                text.send(sender, messagesHolder.get().commands().playerNotFound());
                return CompletableFuture.completedFuture(null);
            }

            return userManager.getOrLoadUser(target.id(), target.name());
        });
    }
}
