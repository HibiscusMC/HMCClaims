package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.task.ResizeDisplayTask;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the selection process for creating or resizing claims.
 */
@Singleton
public class SelectionManager {

    /**
     * Every player currently in an active resize mode, mapped by their unique id.
     * <p>
     * These are the recipients of the persistent resize instructions displayed by
     * the {@link ResizeDisplayTask}.
     */
    private final Map<UUID, Player> resizingPlayers = new ConcurrentHashMap<>();

    private ResizeDisplayTask displayTask;

    @Inject
    private Plugin plugin;

    @Inject
    private BlockMarker blockMarker;

    @Inject
    private ClaimManager claimManager;
    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;
    @Inject
    private ConfigHolder<Settings> settingsHolder;
    @Inject
    private TextUtil text;

    /**
     * Processes a block interaction to update or initiate a claim selection.
     *
     * @param player The player performing the selection.
     * @param block  The block being targeted.
     * @throws IllegalStateException if the user data is missing from the cache.
     */
    public void handleSelection(@NotNull Player player, @NotNull Block block) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));
        Messages messages = messagesHolder.get();

        Location location = block.getLocation();
        BlockSelection blockSelection = BlockSelectionWithY.fromBukkitLocation(location);
        Map<String, String> locationPlaceholder = Map.of(
                "location", blockSelection.x() + ", " + blockSelection.z()
        );

        Claim claim = claimManager.getClaimAt(location)
                .orElse(null);

        Selection selection;
        if (!user.hasActiveSelection()) {
            selection = new Selection(player, blockMarker, claim != null ? claim.main() != null ? claim.main() : claim : null);
            user.currentSelection(selection);
        } else {
            selection = user.currentSelection();
        }

        Claim resizingClaim = selection.resizingClaim();
        boolean isResizing = resizingClaim != null;

        // The player is holding the claiming tool if we got here, so a resize that
        // was still waiting for it can now go live.
        if (isResizing && !selection.active()) {
            activateResize(player, selection);
        }

        if (claim != null) {
            if (isResizing) {
                Claim mainClaim = resizingClaim.main();
                UUID currentClaimId = claim.claimId();
                Claim currentMain = claim.main();

                UUID resizingRootId = (mainClaim != null) ? mainClaim.claimId() : resizingClaim.claimId();
                UUID currentRootId = (currentMain != null) ? currentMain.claimId() : currentClaimId;

                if (!resizingRootId.equals(currentRootId)) {
                    text.send(player, messages.claims().selecting().resizingWrongClaim());
                    return;
                }
            } else if (!claim.owner().equals(player.getUniqueId())) {
                text.send(player, messages.claims().selecting().landAlreadyClaimed());
                return;
            }
        }

        if (isResizing) {
            ClaimRegion originalRegion = resizingClaim.region();
            ClaimRegion originalMainRegion = resizingClaim.main() != null ? resizingClaim.main().region() : null;

            if (selection.points().isEmpty() || selection.points().size() == 2) {
                if (!selection.region().isCorner(blockSelection)) {
                    text.send(player, messages.claims().resizing().selectACorner());
                    return;
                }

                BlockSelection oppositeCorner = selection.region().getOppositeCorner(blockSelection);
                selection.clearPoints();
                selection.region(new ClaimRegion(selection.region().worldName()));

                selection.addBlock(oppositeCorner);
            } else {
                BlockSelection anchor = selection.points().getFirst();

                int hypMinX = Math.min(anchor.x(), blockSelection.x());
                int hypMaxX = Math.max(anchor.x(), blockSelection.x());
                int hypMinZ = Math.min(anchor.z(), blockSelection.z());
                int hypMaxZ = Math.max(anchor.z(), blockSelection.z());

                if ((hypMinX > originalRegion.minX() ||
                        hypMaxX < originalRegion.maxX() ||
                        hypMinZ > originalRegion.minZ() ||
                        hypMaxZ < originalRegion.maxZ())) {
                    text.send(player, messages.claims().resizing().encloseClaimBoundaries());
                    return;
                }

                if (originalMainRegion != null && (hypMinX < originalMainRegion.minX() ||
                        hypMaxX > originalMainRegion.maxX() ||
                        hypMinZ < originalMainRegion.minZ() ||
                        hypMaxZ > originalMainRegion.maxZ())) {
                    text.send(player, messages.claims().resizing().withinMainClaim());
                    return;
                }

                if (claim != null && claim.main() != null && !claim.claimId().equals(resizingClaim.claimId())) {
                    text.send(player, messages.claims().resizing().subWithinSub());
                    return;
                }

                selection.addBlock(blockSelection);
            }

            return;
        }

        boolean isSubClaim = claim != null && claim.main() != null;
        if (isSubClaim) {
            text.send(player, messages.claims().selecting().claimWithinSub());
            return;
        }

        if (selection.main() != null && !selection.main().equals(claim)) {
            text.send(player, messages.claims().selecting().subOutsideBoundaries());
            return;
        }

        if (selection.hasBlock(blockSelection)) {
            boolean isEmpty = selection.removeBlock(blockSelection);

            if (isEmpty) {
                user.currentSelection(null);
                text.send(player, messages.claims().selecting().selectionRemoved());
            } else {
                text.send(player, messages.claims().selecting().cornerUnselected(), locationPlaceholder);
            }

            return;
        }

        InvalidSelectionReason reason = validateSelection(player.getUniqueId(), selection, blockSelection, location);
        switch (reason) {
            case TOO_SMALL -> {
                text.send(player, messages.claims().selecting().selectionTooSmall());
                return;
            }
            case OVERLAPPING -> {
                text.send(player, messages.claims().selecting().claimOverlaps());
                return;
            }
            case NONE -> selection.addBlock(blockSelection);
        }

        if (selection.points().size() == 1) {
            text.send(player, messages.claims().selecting().firstSelection(), locationPlaceholder);
        } else {
            text.send(player, messages.claims().selecting().secondSelection(), locationPlaceholder);
        }
    }

    /**
     * Puts a player into resize mode for the given claim.
     * <p>
     * The session is created immediately, but it only becomes live once the player
     * holds the claiming tool. Until then no markers are shown and no corner can be
     * grabbed, so players are never dropped into a mode they cannot use.
     *
     * @param player The player resizing the claim.
     * @param claim  The claim being resized.
     */
    public void beginResize(@NotNull Player player, @NotNull Claim claim) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));

        // Drop whatever the player had going on, so no stale markers are left behind.
        destroySelection(player);

        Selection selection = new Selection(player, blockMarker, claim.main(), claim.region(), claim);
        user.currentSelection(selection);

        Messages.Claims.Resizing messages = messagesHolder.get().claims().resizing();
        Map<String, String> placeholder = Map.of("name", claim.name());

        if (isHoldingClaimTool(player)) {
            activateResize(player, selection);
        } else {
            text.send(player, messages.grabClaimTool(), placeholder);
        }

        text.send(player, messages.tutorial(), placeholder);
    }

    /**
     * Makes a pending resize session live: markers are rendered and the persistent
     * on-screen instructions start being displayed.
     *
     * @param player    The player resizing the claim.
     * @param selection The player's current resize selection.
     */
    public void activateResize(@NotNull Player player, @NotNull Selection selection) {
        Claim resizingClaim = selection.resizingClaim();
        if (resizingClaim == null || selection.active()) {
            return;
        }

        selection.active(true);
        selection.refreshVisuals(selection.points().isEmpty());

        resizingPlayers.put(player.getUniqueId(), player);
        if (displayTask == null) {
            displayTask = ResizeDisplayTask.start(plugin, this, messagesHolder);
        }

        text.send(player, messagesHolder.get().claims().resizing().started(), Map.of(
                "name", resizingClaim.name()
        ));
    }

    /**
     * Activates the resize session of a player, if they have one still waiting for
     * the claiming tool.
     *
     * @param player The player that just grabbed the claiming tool.
     */
    public void activatePendingResize(@NotNull Player player) {
        Selection selection = currentResize(player);
        if (selection == null || selection.active()) {
            return;
        }

        activateResize(player, selection);
    }

    /**
     * Pauses a resize session without discarding it.
     * <p>
     * Used when the player stops holding the claiming tool: the selection is kept
     * so they can pick up where they left off, but the instructions are hidden.
     *
     * @param player    The player resizing the claim.
     * @param selection The player's current resize selection.
     */
    public void suspendResize(@NotNull Player player, @NotNull Selection selection) {
        if (selection.resizingClaim() == null || !selection.active()) {
            return;
        }

        selection.active(false);
        clearResizeDisplay(player);
    }

    /**
     * Leaves resize mode entirely, discarding the session and its markers.
     *
     * @param player The player leaving resize mode.
     * @return {@code true} if the player was resizing a claim.
     */
    public boolean cancelResize(@NotNull Player player) {
        if (currentResize(player) == null) {
            return false;
        }

        destroySelection(player);
        text.send(player, messagesHolder.get().claims().resizing().cancelled());

        return true;
    }

    /**
     * Retrieves the resize session of a player.
     *
     * @param player The player to check.
     * @return The player's {@link Selection} if they are resizing a claim, otherwise {@code null}.
     */
    @Nullable
    public Selection currentResize(@NotNull Player player) {
        User user = userManager.getUser(player.getUniqueId())
                .orElse(null);

        if (user == null || !user.hasActiveSelection()) {
            return null;
        }

        Selection selection = user.currentSelection();
        return selection.resizingClaim() == null ? null : selection;
    }

    /**
     * @return An {@link Audience} of every player currently in an active resize mode.
     */
    @NotNull
    public Audience resizingAudiences() {
        return Audience.audience(resizingPlayers.values());
    }

    /**
     * Stops displaying the resize instructions to a player and shuts the display task
     * down once nobody is resizing anymore.
     *
     * @param player The player to clear the display for.
     */
    public void clearResizeDisplay(@NotNull Player player) {
        if (resizingPlayers.remove(player.getUniqueId()) == null) {
            return;
        }

        if (displayTask == null) {
            return;
        }

        displayTask.clear(player);

        if (resizingPlayers.isEmpty()) {
            displayTask.cancel();
            displayTask = null;
        }
    }

    /**
     * Checks if the player is holding the configured claiming tool in their main hand.
     *
     * @param player The player to check.
     * @return {@code true} if the main hand holds a valid claiming tool.
     */
    public boolean isHoldingClaimTool(@NotNull Player player) {
        return isClaimTool(player.getInventory().getItemInMainHand());
    }

    /**
     * Checks if the item stack is a valid claiming tool.
     *
     * @param tool The item to check.
     * @return {@code true} if the item is the configured claiming tool.
     */
    public boolean isClaimTool(@Nullable ItemStack tool) {
        if (tool == null || tool.getType() == Material.AIR) {
            return false;
        }

        Settings.Claiming claiming = settingsHolder.get().claiming();

        if (claiming.claimToolStrict()) {
            return tool.isSimilar(claiming.claimTool());
        }

        return Hooks.getStringItem(tool).equalsIgnoreCase(Hooks.getStringItem(claiming.claimTool()));
    }

    /**
     * Checks if a player currently has an active selection session.
     *
     * @param player The player to check.
     * @return {@code true} if a selection is in progress.
     */
    public boolean hasSelection(Player player) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));

        return user.hasActiveSelection();
    }

    /**
     * Terminate the player's selection session and clear all visual markers.
     *
     * @param player The player whose selection should be destroyed.
     */
    public void destroySelection(Player player) {
        clearResizeDisplay(player);

        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));

        if (!user.hasActiveSelection()) {
            return;
        }

        Selection selection = user.currentSelection();
        selection.clearPoints();

        user.currentSelection(null);
    }

    /**
     * Internal helper to validate selection bounds before they are committed.
     *
     * @return The {@link InvalidSelectionReason} describing the failure, or {@code NONE} if valid.
     */
    private InvalidSelectionReason validateSelection(UUID playerId, Selection selection, BlockSelection blockSelection, Location location) {
        if (selection.isTooSmall(blockSelection)) {
            return InvalidSelectionReason.TOO_SMALL;
        }

        ClaimRegion region = null;
        if (selection.points().size() == 2) {
            region = selection.region();
        } else if (selection.points().size() == 1) {
            region = new ClaimRegion(location.getWorld().getName(), List.of(
                    selection.points().getFirst(),
                    blockSelection
            ));
        }

        if (region != null && claimManager.isOverlapping(region, playerId, selection.main() != null, null)) {
            return InvalidSelectionReason.OVERLAPPING;
        }

        return InvalidSelectionReason.NONE;
    }
}