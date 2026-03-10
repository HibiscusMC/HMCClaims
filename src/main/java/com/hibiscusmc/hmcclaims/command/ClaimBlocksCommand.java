package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.OptArg;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

@Command(names = {"claimblocks", "cbs"}, permission = "hmcclaims.commands.claimblocks")
public class ClaimBlocksCommand implements CommandClass {

    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

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

}