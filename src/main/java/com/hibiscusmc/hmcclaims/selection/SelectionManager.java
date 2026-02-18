package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private Text text;

    public void handleSelection(Player player, Block block) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));

        Messages messages = messagesHolder.get();

        Location location = block.getLocation();
        BlockSelection blockSelection = BlockSelectionWithY.fromBukkitLocation(location);
        Map<String, String> locationPlaceholder = Map.of(
                "location", blockSelection.x() + ", " + blockSelection.z()
        );

        List<Claim> claims = claimManager.getClaimsAt(location);
        boolean isParent = claims.size() == 1;

        Claim claim = isParent ? claims.getFirst() : null;

        if (claim != null && !claim.owner().uuid().equals(player.getUniqueId())) {
            text.send(player, messages.claims().selecting().landAlreadyClaimed());
            return;
        }

        boolean hasChild = claims.size() > 1;

        if (hasChild) {
            text.send(player, messages.claims().selecting().claimWithinChild());
            return;
        }

        Selection selection;

        if (!user.hasActiveSelection()) {
            selection = new Selection(player, blockMarker, claim);
            user.currentSelection(selection);
        } else {
            selection = user.currentSelection();
        }

        if (selection.parent() != null && !selection.parent().equals(claim)) {
            text.send(player, messages.claims().selecting().childOutsideBoundaries());
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

    public boolean hasSelection(Player player) {
        User user = userManager.getUser(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("User not loaded!"));

        return user.hasActiveSelection();
    }

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

        if (region != null && claimManager.isOverlapping(region, playerId, selection.parent() != null)) {
            return InvalidSelectionReason.OVERLAPPING;
        }

        return InvalidSelectionReason.NONE;
    }
}