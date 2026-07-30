package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import team.unnamed.inject.Inject;

/**
 * Place Blocks, Break Blocks, Use Containers
 */
public class VanillaBlockListener implements Listener {

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseContainer(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Container)) {
            return;
        }

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_CONTAINER)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().interactBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.PLACE_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().placeBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.BREAK_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().breakBlock());
        event.setCancelled(true);
    }
}