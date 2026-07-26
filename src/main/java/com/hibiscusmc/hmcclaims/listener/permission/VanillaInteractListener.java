package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import team.unnamed.inject.Inject;

public class VanillaInteractListener implements Listener {

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        Permission permission = Permission.BREAK_BLOCK;
        boolean hasPermission = claim.getMember(player.getUniqueId())
                .map(member -> member.hasPermission(permission))
                .orElse(claim.roleRegistry().everyoneRole().hasPermission(permission));

        if (hasPermission) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().cantInteract());
    }
}