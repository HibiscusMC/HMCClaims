package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.command.argument.ClaimMember;
import com.hibiscusmc.hmcclaims.command.argument.PlayerOrOffline;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimDeleteGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.minecraft.server.players.NameAndId;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Usage;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.Objects;

@Command(names = {"claim"}, permission = "hmcclaims.commands.claim")
public class ClaimCommand implements CommandClass {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

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

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        ClaimMemberListGui gui = guis.get(ClaimMemberListGui.class);

        gui.open(sender, new GuiMetadata(claim));
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

        ClaimDeleteGui gui = guis.get(ClaimDeleteGui.class);

        gui.open(sender, new GuiMetadata(claim));
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

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (player == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        String playerName = Objects.requireNonNull(player.getName(), "playerName should never be null");
        com.hibiscusmc.hmcclaims.claim.ClaimMember added = claim.addMember(new NameAndId(player.getUniqueId(), playerName));
        if (added == null) {
            text.send(sender, messages.claims().memberAlreadyAdded());
            return;
        }

        Storage storage = storageHolder.get();
        storage.claims()
                .saveMembers(claim);

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

        if (!sender.getUniqueId().equals(claim.owner())) {
            text.send(sender, messages.commands().notYourClaim());
            return;
        }

        if (player == null) {
            text.send(sender, messages.commands().playerNotFound());
            return;
        }

        String playerName = Objects.requireNonNull(player.getName(), "playerName should never be null");
        boolean removed = claim.removeMember(player.getUniqueId());
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
}