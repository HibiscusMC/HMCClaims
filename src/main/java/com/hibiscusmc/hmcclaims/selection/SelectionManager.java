package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
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

import java.util.Map;

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

        Selection selection = user.currentSelection();

        if (!user.hasActiveSelection()) {
            selection = new Selection(player, blockMarker);
            user.currentSelection(selection);
        }

        if (selection.hasBlock(blockSelection)) {
            boolean isEmpty = selection.removeBlock(blockSelection);

            if (isEmpty) {
                user.currentSelection(null);
                text.send(player, messages.claims().selecting().selectionRemoved());
            } else {
                text.send(player, messages.claims().selecting().cornerUnselected(), locationPlaceholder);
            }
        } else {
            InvalidSelectionReason reason = validateSelection(selection, blockSelection, location);

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

            if (selection.points().size() < 2) {
                text.send(player, messages.claims().selecting().firstSelection(), locationPlaceholder);
            } else {
                text.send(player, messages.claims().selecting().secondSelection(), locationPlaceholder);
            }
        }
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

    private InvalidSelectionReason validateSelection(Selection selection, BlockSelection blockSelection, Location location) {
        if (selection.isTooSmall(blockSelection)) {
            return InvalidSelectionReason.TOO_SMALL;
        }

        if (claimManager.getClaimAt(location).isPresent()) {
            return InvalidSelectionReason.OVERLAPPING;
        }

        return InvalidSelectionReason.NONE;
    }
}