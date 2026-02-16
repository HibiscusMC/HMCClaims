package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import team.unnamed.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerSelectionListener implements Listener {

    @Inject
    private BlockMarker marker;

    @Inject
    private SelectionManager selectionManager;
    @Inject
    private ClaimManager claimManager;
    @Inject
    private UserManager userManager;

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

            List<Claim> claims = claimManager.getClaimsAt(block.getLocation());
            if (claims.isEmpty()) {
                return;
            }

            for (Claim claim : claims) {
                if (claims.size() == 1 || claim.parent() != null) {
                    Player owner = Bukkit.getPlayer(claim.owner().uuid());
                    text.send(player, messages.claims().ownedBy(), Map.of(
                            "name", claim.name(),
                            "owner", owner == null ? "unknown" : owner.getName()
                    ));
                }

                if (claim.owner().uuid().equals(player.getUniqueId())) {
                    marker.mark(player, claim.region().getLCornerBlocks(), claim.parent() != null ? MarkType.INSPECT_CHILD : MarkType.INSPECT, TimeUnit.SECONDS.toMillis(5));
                } else {
                    marker.mark(player, claim.region().getLCornerBlocks(), claim.parent() != null ? MarkType.INSPECT_CHILD_OTHER : MarkType.INSPECT_OTHER, TimeUnit.SECONDS.toMillis(5));
                }
            }

            return;
        }

        if (itemInHand.getType() != settings.claiming().claimTool()) {
            return;
        }

        event.setCancelled(true);

        if (action.isRightClick()) {
            selectionManager.handleSelection(player, block);
        } else {
            User user = userManager.getUser(player.getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("User not loaded!"));

            if (!user.hasActiveSelection()) {
                text.send(player, messages.claims().selecting().mustSelectRegion());
                return;
            }

            Selection selection = user.currentSelection();

            if (selection.points().size() < 2) {
                text.send(player, messages.claims().selecting().mustSelectPoints());
                return;
            }

            ClaimRegion region = selection.region();

            if (claimManager.isOverlapping(region)) {
                text.send(player, messages.claims().selecting().claimOverlaps());
                return;
            }

            if (selection.parent() == null) {
                long surfaceArea = region.getSurfaceArea();
                long currentBlocks = userManager.getRemainingBlocks(user);

                if (surfaceArea > currentBlocks) {
                    text.send(player, messages.claims().selecting().notEnoughClaimBlocks(), Map.of(
                            "required_blocks", surfaceArea + "",
                            "current_blocks", currentBlocks + ""
                    ));

                    return;
                }

                Claim claim = claimManager.createClaim(player, region, selection.parent());
                selectionManager.destroySelection(player);

                text.send(player, messages.claims().created(), Map.of(
                        "name", claim.name(),
                        "price", surfaceArea + ""
                ));

                marker.mark(player, claim.region().getLCornerBlocks(), MarkType.CREATE, TimeUnit.SECONDS.toMillis(10));
            } else {
                Claim claim = claimManager.createClaim(player, region, selection.parent());
                selectionManager.destroySelection(player);

                text.send(player, messages.claims().childCreated(), Map.of(
                        "name", claim.name()
                ));

                marker.mark(player, claim.region().getLCornerBlocks(), MarkType.CREATE_CHILD, TimeUnit.SECONDS.toMillis(10));
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        Settings holder = settingsHolder.get();

        if (item == null || item.getType() != holder.claiming().claimTool()) {
            removeSelection(player);
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