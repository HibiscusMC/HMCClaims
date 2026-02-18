package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.command.argument.PlayerOrOffline;
import com.hibiscusmc.hmcclaims.config.Guis;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import com.hibiscusmc.hmcclaims.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Command(names = "hmcclaims", permission = "hmcclaims.admin")
public class HMCClaimsCommand implements CommandClass {

    @Inject
    private Set<Service> services;

    @Inject
    private ConfigHolder<Settings> settings;
    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private GuiRegistry guis;

    @Inject
    private UserManager userManager;
    @Inject
    private ClaimManager claimManager;

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

            ConfigFactory.reload(Guis.ClaimList.class);
            ConfigFactory.reload(Guis.ClaimInfo.class);

            guis.reload();

            text.send(sender, messages.get().pluginReload());
        } catch (Exception e) {
            sender.sendRichMessage("<red>Plugin reload failed! See console for more information.");
            e.printStackTrace();
        }
    }

    @Command(names = "test")
    public void test(@Sender Player sender) {
        ClaimListGui gui = guis.get(ClaimListGui.class);

        gui.open(sender);
    }

    @Command(names = "testadd")
    public void testadd(@Sender Player sender, @PlayerOrOffline OfflinePlayer player) {
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
                "player_head", "<head:" + player.getUniqueId() + ">",
                "name", player.getName(),
                "claim", claim.name()
        ));
    }
}