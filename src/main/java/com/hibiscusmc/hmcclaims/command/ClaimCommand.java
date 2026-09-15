package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.command.argument.Member;
import com.hibiscusmc.hmcclaims.command.argument.PlayerName;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimDeleteGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.menu.MenuService;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.PlayerResolver;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

import java.util.Map;

@Command(names = {"claim"}, permission = "hmcclaims.commands.claim")
public class ClaimCommand implements CommandClass {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private MenuService menus;

    @Inject
    private SelectionManager selectionManager;

    @Inject
    private PlayerResolver playerResolver;

    @Inject
    private TextUtil text;

    @Command(names = {"", "info"})
    public void info(@Sender Player sender) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        menus.open(ClaimMemberListGui.class, sender, new GuiMetadata(claim));
    }

    @Command(names = {"delete"})
    public void delete(@Sender Player sender) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        menus.open(ClaimDeleteGui.class, sender, new GuiMetadata(claim));
    }

    @Command(names = {"add", "trust"})
    @Usage("<player>")
    public void add(@Sender Player sender, @PlayerName String playerName) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        playerResolver.resolve(playerName, player -> {
            if (player == null) {
                text.send(sender, messages.commands().playerNotFound());
                return;
            }

            ClaimMember added = claim.addMember(player);
            if (added == null) {
                text.send(sender, messages.claims().memberAlreadyAdded());
                return;
            }

            Storage storage = storageHolder.get();
            storage.claims()
                    .saveMembers(claim);

            text.send(sender, messages.claims().memberAdded(), Map.of(
                    "player_head", "<head:" + player.name() + ">",
                    "name", player.name(),
                    "claim", claim.name()
            ));
        });
    }

    @Command(names = {"remove", "untrust"}, permission = "hmcclaims.commands.claim.remove")
    @Usage("<claim member>")
    public void remove(@Sender Player sender, @Member ClaimMember member) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (member == null) {
            text.send(sender, messages.claims().playerNotMember());
            return;
        }

        if (member.isOwner()) {
            text.send(sender, messages.claims().cantRemoveOwner());
            return;
        }

        String playerName = member.lastKnownName();
        boolean removed = claim.removeMember(member.uuid());
        if (!removed) {
            text.send(sender, messages.claims().playerNotMember());
            return;
        }

        Storage storage = storageHolder.get();
        storage.claims()
                .saveMembers(claim);

        text.send(sender, messages.claims().memberRemoved(), Map.of(
                "player_head", "<head:" + playerName + ">",
                "name", playerName,
                "claim", claim.name()
        ));
    }

    /**
     * Leaves resize mode. Backs the clickable cancel button of the resize tutorial.
     */
    @Command(names = {"cancelresize", "resizecancel"})
    public void cancelResize(@Sender Player sender) {
        if (!selectionManager.cancelResize(sender)) {
            text.send(sender, messagesHolder.get().claims().resizing().notResizing());
        }
    }
}