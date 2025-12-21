package com.hibiscusmc.hmcclaims.manager;

import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.selection.BlockSelection;
import com.hibiscusmc.hmcclaims.selection.BlockSelectionWithY;
import com.hibiscusmc.hmcclaims.selection.InvalidSelectionReason;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class SelectionManager {

    @Inject
    private BlockMarker blockMarker;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private Text text;

    private final Map<UUID, Selection> selections = new HashMap<>();

    public void handleSelection(Player player, Block block) {
        UUID uuid = player.getUniqueId();
        Selection selection = selections.get(uuid);

        Messages messages = messagesHolder.get();

        Location location = block.getLocation();
        BlockSelection blockSelection = BlockSelectionWithY.fromBukkitLocation(location);
        Map<String, String> locationPlaceholder = Map.of(
                "location", blockSelection.x() + ", " + blockSelection.z()
        );

        if (selection == null) {
            selection = new Selection(player, blockMarker);
            selections.put(uuid, selection);
        }

        if (selection.hasBlock(blockSelection)) {
            boolean isEmpty = selection.removeBlock(blockSelection);

            if (isEmpty) {
                selections.remove(uuid);
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

    public Selection fetchSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void destroySelection(Player player) {
        UUID uuid = player.getUniqueId();
        Selection selection = selections.get(uuid);

        if (selection == null) {
            return;
        }

        selection.clearPoints();

        selections.remove(uuid);


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