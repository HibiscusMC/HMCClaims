package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Represents a player's active area selection process.
 */
public class Selection {

    private final BlockMarker marker;
    private final Player player;

    /**
     * A double-ended queue holding the current selection points.
     * Limits the selection to exactly two points for rectangular area definition.
     */
    private final Deque<BlockSelection> points = new ArrayDeque<>(2);

    @Getter
    private final Claim main;

    @Getter
    private final Claim resizingClaim;

    @Setter
    @Getter
    private ClaimRegion region;

    public Selection(Player player, BlockMarker marker, Claim main) {
        this(player, marker, main, null, null);
    }

    /**
     * Constructs a new selection session for a player.
     *
     * @param player        The player making the selection.
     * @param marker        The visual handler for showing block markers.
     * @param main          The parent claim (if creating a sub-claim), or {@code null} for a primary claim.
     * @param region        The pre-defined region of the selection, or {@code null} if it's a new selection.
     * @param resizingClaim The claim this selection is resizing, or {@code null} if it's not resizing any claim.
     */
    public Selection(Player player, BlockMarker marker, Claim main, ClaimRegion region, Claim resizingClaim) {
        this.marker = marker;
        this.player = player;
        this.main = main;
        this.resizingClaim = resizingClaim;

        this.region = Objects.requireNonNullElseGet(region, () ->
                new ClaimRegion(player.getWorld().getName())
        );
    }

    /**
     * Retrieves the current selection points as a list.
     *
     * @return A list of 0, 1, or 2 {@link BlockSelection} points.
     */
    @NotNull
    @Contract(pure = true)
    public List<BlockSelection> points() {
        return points.stream().toList();
    }

    /**
     * Adds a point to the selection.
     * If two points already exist, the oldest point is removed to make room.
     *
     * @param block The new coordinate to add.
     */
    public void addBlock(@NotNull BlockSelection block) {
        if (points.size() >= 2) {
            BlockSelection old = points.pollFirst();
            region.removeCorner(old);
        }

        points.add(block);
        region.addCorner(block);

        refreshVisuals(false);
    }

    /**
     * Removes a specific point from the selection.
     *
     * @param block The coordinate to remove.
     * @return {@code true} if the selection is now empty.
     */
    public boolean removeBlock(@NotNull BlockSelection block) {
        if (points.isEmpty()) {
            return true;
        }

        points.remove(block);
        region.removeCorner(block);
        refreshVisuals(false);

        return points.isEmpty();
    }

    /**
     * Checks if the current selection already contains the specified block
     *
     * @param block The coordinate to check for
     * @return {@code true} if the selection contains the specified block
     */
    public boolean hasBlock(BlockSelection block) {
        return points.contains(block);
    }

    /**
     * Validates if the selection meets minimum size requirements relative to a new point.
     * <p>
     * Current hardcoded threshold: 5x5 blocks.
     *
     * @param corner2 The second point to check against the existing point.
     * @return {@code true} if either dimension is less than 5 blocks.
     */
    public boolean isTooSmall(BlockSelection corner2) {
        if (points.isEmpty()) {
            return false;
        }

        BlockSelection corner1 = points.peekLast();

        int length = Math.abs(corner1.x() - corner2.x()) + 1;
        int width = Math.abs(corner1.z() - corner2.z()) + 1;

        return length < 5 || width < 5;
    }

    /**
     * Updates the visual block markers for the player based on current points.
     *
     * @param preferRegion If it should use region corners explicitly instead of relying on the selection points.
     */
    public void refreshVisuals(boolean preferRegion) {
        marker.clearAllMarks(player.getUniqueId());

        MarkType type = main == null ?
                resizingClaim != null ? MarkType.RESIZE : MarkType.SELECT :
                resizingClaim != null ? MarkType.RESIZE_SUB : MarkType.SELECT_SUB;

        if (preferRegion || points.size() == 2) {
            marker.mark(player, region.getLCornerBlocks(), type,
                    0);
        } else if (!points.isEmpty()) {
            marker.mark(player, List.of(points.peekFirst()), type,
                    0);
        }
    }

    /**
     * Resets the selection and clears all active visual markers.
     */
    public void clearPoints() {
        points.clear();
        marker.clearAllMarks(player.getUniqueId());
    }
}