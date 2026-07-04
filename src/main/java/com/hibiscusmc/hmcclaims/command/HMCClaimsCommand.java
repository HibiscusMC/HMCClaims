package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

import java.util.Set;

@Command(names = {"hmcclaims", "claimsadmin"}, permission = "hmcclaims.commands.admin")
public class HMCClaimsCommand implements CommandClass {

    @Inject
    private Set<Service> services;

    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private GuiRegistry guis;

    @Inject
    private TextUtil text;

    @Command(names = "reload", permission = "hmcclaims.commands.admin.reload")
    public void reload(CommandSender sender) {
        for (Service service : services) {
            service.reload();
        }

        try {
            for (Class<?> clazz : ConfigFactory.all(false)) {
                ConfigFactory.reload(clazz);
            }

            guis.reload();

            text.send(sender, messages.get().pluginReload());
        } catch (Exception e) {
            sender.sendRichMessage("<red>Plugin reload failed! See console for more information.");
            Logger.error("Plugin reload failed!", e);
        }
    }

    @Command(names = {"createtest"}, permission = "hmcclaims.commands.admin.createtest")
    public void createTest(@Sender Player player) {
        String worldName = player.getWorld().getName();
        Chunk chunk = player.getLocation().getChunk();

        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;

        ClaimRegion region1 = new ClaimRegion(worldName, minX, minX + 7, minZ, minZ + 7);
        ClaimRegion region2 = new ClaimRegion(worldName, minX + 8, minX + 15, minZ, minZ + 7);
        ClaimRegion region3 = new ClaimRegion(worldName, minX, minX + 7, minZ + 8, minZ + 15);
        ClaimRegion region4 = new ClaimRegion(worldName, minX + 8, minX + 15, minZ + 8, minZ + 15);

        claimManager.createClaim(player, region1, null);
        claimManager.createClaim(player, region2, null);
        claimManager.createClaim(player, region3, null);
        claimManager.createClaim(player, region4, null);
    }
}