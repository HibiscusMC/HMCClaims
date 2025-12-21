package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.manager.ClaimManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.manager.SelectionManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerSelectionListener extends AbstractListener {

    @Inject
    private Plugin plugin;

    @Inject
    private BlockMarker marker;

    @Inject
    private SelectionManager selectionManager;
    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Settings> settingsHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private Text text;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Settings settings = settingsHolder.get();
        Messages messages = messagesHolder.get();

        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItem();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            if (!player.isSneaking()) {
                return;
            }

            Claim claim = claimManager.getClaimAt(block.getLocation())
                    .orElse(null);
            if (claim == null) {
                return;
            }

            Player owner = Bukkit.getPlayer(claim.owner());
            text.send(owner, messages.claims().ownedBy(), Map.of(
                    "name", claim.name(),
                    "owner", owner.getName()
            ));

            marker.mark(player, claim.region().getLCornerBlocks(), MarkType.INSPECT, TimeUnit.SECONDS.toMillis(5));
            return;
        }

        if (itemInHand.getType() != settings.claiming().claimTool()) {
            return;
        }

        event.setCancelled(true);

        if (action.isRightClick()) {
            selectionManager.handleSelection(player, block);
        } else {
            Selection selection = selectionManager.fetchSelection(player);
            if (selection == null) {
                text.send(player, messages.claims().selecting().mustSelectRegion());
                return;
            }

            if (selection.points().size() < 2) {
                text.send(player, messages.claims().selecting().mustSelectPoints());
                return;
            }

            ClaimRegion region = selection.region();
            if (claimManager.isOverlapping(region)) {
                text.send(player, messages.claims().selecting().claimOverlaps());
                return;
            }

            Claim claim = claimManager.createClaim(player, selection.region());
            selectionManager.destroySelection(player);

            long surfaceArea = claim.region().getSurfaceArea();
            text.send(player, messages.claims().created(), Map.of(
                    "name", claim.name(),
                    "price", surfaceArea + ""
            ));

            marker.mark(player, claim.region().getLCornerBlocks(), MarkType.CREATE, TimeUnit.SECONDS.toMillis(10));
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        removeSelection(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        removeSelection(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        selectionManager.destroySelection(event.getPlayer());
    }

    private void removeSelection(Player player) {
        selectionManager.destroySelection(player);
        text.send(player, messagesHolder.get().claims().selecting().selectionRemoved());
    }

}