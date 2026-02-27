package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.command.argument.PlayerOrOffline;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import com.hibiscusmc.hmcclaims.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.stream.Collectors;

@Command(names = {"claims", "claim"}, permission = "hmcclaims.commands.claim")
public class ClaimsCommand implements CommandClass {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private GuiRegistry guis;

    @Inject
    private TextUtil text;

    @Command(names = {"", "list"})
    public void list(@Sender Player sender) {
        ClaimListGui gui = guis.get(ClaimListGui.class);

        gui.open(sender);
    }

    @Command(names = {"add", "trust"})
    public void add(@Sender Player sender, @PlayerOrOffline OfflinePlayer player) {
        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, "<red>You're not standing on a claim!");
            return;
        }

        if (player == null) {
            text.send(sender, "<red>Player not found!");
            return;
        }

        ClaimMember member = new ClaimMember(
                player.getUniqueId(),
                claim,
                player.getName(),
                claim.roleRegistry().defaultRole(),
                claim.roleRegistry().defaultRole().permissions()
                        .stream().map(permission -> new PermissionHolder(permission, true))
                        .collect(Collectors.toUnmodifiableSet())
        );

        claim.addMember(member);

        text.send(sender, "<green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!", Map.of(
                "player_head", "<head:" + player.getName() + ">",
                "name", player.getName(),
                "claim", claim.name()
        ));
    }

}