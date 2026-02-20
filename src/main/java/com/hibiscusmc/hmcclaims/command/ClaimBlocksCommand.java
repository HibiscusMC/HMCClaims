package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.OptArg;
import team.unnamed.inject.Inject;

import java.util.Map;

@Command(names = {"claimblocks", "cbs"})
public class ClaimBlocksCommand implements CommandClass {

    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Settings> settingsHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private Text text;

    @Command(names = {""})
    public void base(CommandSender sender, @OptArg Player player) {
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        Settings settings = settingsHolder.get();
        Messages messages = messagesHolder.get();

        if (player == null) {
            text.send(sender, messages.commands().missingPlayer());
            return;
        }

        User user = userManager.getUser(player.getUniqueId()).orElse(null);
        long startingBlocks = settings.claimBlocks().startingAmount();
        long obtainedBlocks = user != null ? user.claimBlocks() : 0;
        long totalBlocks = startingBlocks + obtainedBlocks;
        long availableBlocks = userManager.getRemainingBlocks(user);
        long usedBlocks = totalBlocks - availableBlocks;

        text.send(sender, messages.commands().claimBlocks().summary(), Map.of(
                "starting_blocks", startingBlocks + "",
                "obtained_blocks", obtainedBlocks + "",
                "total_blocks", totalBlocks + "",
                "used_blocks", usedBlocks + "",
                "available_blocks", availableBlocks + "",
                "player_name", player.getName()
        ));
    }

}