package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates the selection process for creating or resizing claims.
 */
@Singleton
public class SelectionManager {

    @Inject
    private BlockMarker blockMarker;

    @Inject
    private ClaimManager claimManager;
    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;
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