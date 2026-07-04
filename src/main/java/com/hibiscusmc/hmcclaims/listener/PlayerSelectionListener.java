package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
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

/**
 * Handles all player-driven interactions related to claim selection and inspection.
 */
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
    private StorageHolder storageHolder;

    @Inject
    private SchedulerUtil scheduler;
    @Inject
    private TextUtil text;

    /**
     * Entry point for claim interactions. Dispatches to inspection or selection
     * logic based on the player's held item and sneaking state.
     *
     * @param event The interaction event.
     */
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

            scheduler.scheduleAsync(() -> {
                List<Claim> claims = claimManager.getClaimsAt(block.getLocation());
                if (claims.isEmpty()) {
                    return;
                }

                for (Claim claim : claims) {
                    if (claims.size() == 1 || claim.main() != null) {
                        Player owner = Bukkit.getPlayer(claim.owner());
                        text.send(player, messages.claims().ownedBy(), Map.of(
                                "name", claim.name(),
                                "owner", owner == null ? "unknown" : owner.getName()
                        ));
                    }

                    if (claim.owner().equals(player.getUniqueId())) {
                        marker.mark(player, claim.region().getLCornerBlocks(), claim.main() != null ? MarkType.INSPECT_SUB : MarkType.INSPECT, TimeUnit.SECONDS.toMillis(5));
                    } else {
                        marker.mark(player, claim.region().getLCornerBlocks(), claim.main() != null ? MarkType.INSPECT_SUB_OTHER : MarkType.INSPECT_OTHER, TimeUnit.SECONDS.toMillis(5));
                    }
                }
            });

            return;
        }

        if (!itemInHand.isSimilar(settings.claiming().claimTool())) {
            return;
        }

        event.setCancelled(true);

        boolean isRightClick = action.isRightClick();
        scheduler.scheduleAsync(() -> {
            if (isRightClick) {
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

                if (claimManager.isOverlapping(region, player.getUniqueId(), selection.main() != null, selection.resizingClaim())) {
                    text.send(player, messages.claims().selecting().claimOverlaps());
                    return;
                }

                Claim resizingClaim = selection.resizingClaim();

                Storage storage = storageHolder.get();
                if (selection.main() == null) {
                    long surfaceArea = region.getSurfaceArea();
                    long currentBlocks = userManager.getRemainingBlocks(user);
                    long diff = surfaceArea - (resizingClaim == null ? 0 : resizingClaim.region().getSurfaceArea());

                    if (diff > currentBlocks) {
                        text.send(player, messages.claims().selecting().notEnoughClaimBlocks(), Map.of(
                                "required_blocks", diff + "",
                                "current_blocks", currentBlocks + ""
                        ));

                        return;
                    }

                    Claim claim = getClaimAndUpdate(player, selection, region, resizingClaim, storage);

                    text.send(player, resizingClaim == null ? messages.claims().created() : messages.claims().resized(), Map.of(
                            "name", claim.name(),
                            "price", diff + ""
                    ));

                    marker.mark(player, claim.region().getLCornerBlocks(), MarkType.CREATE, TimeUnit.SECONDS.toMillis(10));
                } else {
                    Claim claim = getClaimAndUpdate(player, selection, region, resizingClaim, storage);

                    text.send(player, resizingClaim == null ? messages.claims().subCreated() : messages.claims().subResized(), Map.of(
                            "name", claim.name()
                    ));

                    marker.mark(player, claim.region().getLCornerBlocks(), MarkType.CREATE_SUB, TimeUnit.SECONDS.toMillis(10));
                }

                userManager.calculateUsedBlocks(player.getUniqueId());
            }
        });
    }

    /**
     * Clears active selections if the player switches away from the claiming tool.
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        Settings holder = settingsHolder.get();
        ItemStack claimTool = holder.claiming().claimTool();

        if ((newItem == null || !newItem.isSimilar(claimTool)) &&
                (oldItem != null && oldItem.isSimilar(claimTool))) {
            removeSelection(player, false);
        }
    }

    /**
     * Ensures visual markers and selections are cleared when a player
     * teleports away.
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        removeSelection(event.getPlayer(), true);
    }

    /**
     * Ensures visual markers and selections are cleared when a player
     * changes worlds.
     */
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        removeSelection(event.getPlayer(), true);
    }

    /**
     * Final cleanup of user selection data upon disconnection to prevent memory leaks.
     */
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        selectionManager.destroySelection(event.getPlayer());
    }

    /**
     * Retrieves an existing claim being resized, or creates a new claim if none is provided,
     * while updating the claim's region, cache, and persistent storage.
     * <p>
     * If {@code resizingClaim} is present, its region is updated to the new {@code region} and
     * it is re-added to the cache and saved to the {@code storage} (if available). Finally,
     * the player's active visual selection is destroyed.
     * </p>
     *
     * @param player        The {@link Player} who owns or is modifying the claim.
     * @param selection     The current {@link Selection} context containing selection data.
     * @param region        The new {@link ClaimRegion} boundaries to apply to the claim.
     * @param resizingClaim The existing {@link Claim} currently being resized, or {@code null}
     *                      if a new claim should be created.
     * @param storage       The {@link Storage} system used to persist claim data.
     * @return The newly created or updated {@link Claim} instance.
     */
    private Claim getClaimAndUpdate(Player player, Selection selection, ClaimRegion region, Claim resizingClaim, Storage storage) {
        Claim claim = resizingClaim == null ? claimManager.createClaim(player, region, selection.main()) : resizingClaim;
        if (resizingClaim != null) {
            resizingClaim.region(region);
            claimManager.addClaimToCache(claim);

            ClaimRepository claimRepository = storage.claims();
            claimRepository.saveClaim(claim);
        }

        selectionManager.destroySelection(player);
        return claim;
    }

    /**
     * Utility method to reset a player's selection state and notify them.
     *
     * @param player The player to reset.
     * @param force  {@code true} if the action triggering the method requires the selection to
     *               be removed, {@code false} if it's a soft removal (like switching the held item).
     *               <br>A soft removal (passing a {@code false} value) won't remove the selection if the player
     *               is resizing the claim.
     */
    private void removeSelection(Player player, boolean force) {
        if (selectionManager.hasSelection(player)) {
            if (!force) {
                User user = userManager.getUser(player.getUniqueId())
                        .orElseThrow(() -> new IllegalStateException("User not loaded!"));

                Selection selection = user.currentSelection();
                if (selection == null) {
                    return;
                }

                if (selection.resizingClaim() != null) {
                    return;
                }
            }

            selectionManager.destroySelection(player);
            text.send(player, messagesHolder.get().claims().selecting().selectionRemoved());
        }
    }
}