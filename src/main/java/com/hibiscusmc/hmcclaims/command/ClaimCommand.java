package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.command.argument.ClaimMember;
import com.hibiscusmc.hmcclaims.command.argument.PlayerOrOffline;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.minecraft.server.players.NameAndId;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.OptArg;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Command(names = {"claim"}, permission = "hmcclaims.commands.claim")
public class ClaimCommand implements CommandClass {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private GuiRegistry guis;

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

        if (!sender.getUniqueId().equals(claim.owner().uuid())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        ClaimMemberListGui gui = guis.get(ClaimMemberListGui.class);

        gui.open(sender, claim);
    }

    @Command(names = {"delete"})
    public void delete(@Sender Player sender, @OptArg String confirmationId) {
        Messages messages = messagesHolder.get();

        String claimId = null;
        if (confirmationId != null && !confirmationId.isEmpty()) {
            String[] parts = confirmationId.split(";");
            if (parts.length != 2 || !parts[0].equals("confirm")) {
                return;
            }

            claimId = parts[1];
        }

        Claim claim;
        if (claimId != null) {
            claim = claimManager.getClaim(UUID.fromString(claimId))
                    .orElse(null);
        } else {
            claim = claimManager.getClaimAt(sender.getLocation())
                    .orElse(null);
        }

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner().uuid())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (confirmationId == null || confirmationId.isEmpty()) {
            text.send(sender, messages.commands().deleteConfirm(), Map.of(
                    "claim_name", claim.name(),
                    "claim_id", claim.claimId().toString()
            ));
        } else {
            claimManager.deleteClaim(claim);
            text.send(sender, messages.commands().deleteSuccess());
        }
    }

    @Command(names = {"add", "trust"}, permission = "hmcclaims.commands.claim.add")
    @Usage("<player>")
    public void add(@Sender Player sender, @PlayerOrOffline OfflinePlayer player) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner().uuid())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (player == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        String playerName = Objects.requireNonNull(player.getName(), "playerName should never be null");
        boolean added = claim.addMember(new NameAndId(player.getUniqueId(), playerName));
        if (!added) {
            text.send(sender, messages.claims().memberAlreadyAdded());
            return;
        }

        text.send(sender, messages.claims().memberAdded(), Map.of(
                "player_head", "<head:" + playerName + ">",
                "name", playerName,
                "claim", claim.name()
        ));
    }

    @Command(names = {"remove", "untrust"}, permission = "hmcclaims.commands.claim.remove")
    @Usage("<claim member>")
    public void remove(@Sender Player sender, @ClaimMember OfflinePlayer player) {
        Messages messages = messagesHolder.get();

        Claim claim = claimManager.getClaimAt(sender.getLocation())
                .orElse(null);

        if (claim == null) {
            text.send(sender, messages.commands().notInClaim());
            return;
        }

        if (!sender.getUniqueId().equals(claim.owner().uuid())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (player == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        String playerName = Objects.requireNonNull(player.getName(), "playerName should never be null");
        boolean added = claim.removeMember(player.getUniqueId());
        if (!added) {
            text.send(sender, messages.claims().playerNotMember());
            return;
        }

        text.send(sender, messages.claims().memberRemoved(), Map.of(
                "player_head", "<head:" + playerName + ">",
                "name", playerName,
                "claim", claim.name()
        ));
    }
}