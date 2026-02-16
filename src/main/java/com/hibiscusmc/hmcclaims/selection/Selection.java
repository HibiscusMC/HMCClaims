package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Selection {

    private final BlockMarker marker;
    private final Player player;

    private final Deque<BlockSelection> points = new ArrayDeque<>(2);

    @Getter
    private final ClaimRegion region;
    @Getter
    private final Claim parent;

    public Selection(Player player, BlockMarker marker, Claim parent) {
        this.marker = marker;
        this.player = player;
        this.parent = parent;

        this.region = new ClaimRegion(player.getWorld().getName());
    }

    public List<BlockSelection> points() {
        return points.stream().toList();
    }

    public void addBlock(BlockSelection block) {
        if (points.size() >= 2) {
            BlockSelection old = points.pollFirst();
            region.removeCorner(old);
        }

        points.add(block);
        region.addCorner(block);

        refreshVisuals();
    }

    public boolean removeBlock(BlockSelection block) {
        if (points.isEmpty()) {
            return true;
        }

        points.remove(block);
        region.removeCorner(block);
        refreshVisuals();

        return points.isEmpty();
    }

    public boolean hasBlock(BlockSelection block) {
        return points.contains(block);
    }


    public boolean isTooSmall(BlockSelection corner2) {
        if (points.isEmpty()) {
            return false;
        }

        BlockSelection corner1 = points.peekLast();

        int length = Math.abs(corner1.x() - corner2.x()) + 1;
        int width = Math.abs(corner1.z() - corner2.z()) + 1;

        return length < 5 || width < 5;
    }

    public void refreshVisuals() {
        marker.clearAllMarks(player.getUniqueId());

        if (points.size() == 2) {
            marker.mark(player, region.getLCornerBlocks(), parent == null ? MarkType.SELECT : MarkType.SELECT_CHILD, 0);
        } else if (!points.isEmpty()) {
            marker.mark(player, List.of(points.peekFirst()), parent == null ? MarkType.SELECT : MarkType.SELECT_CHILD, 0);
        }
    }

    public void clearPoints() {
        points.clear();
        marker.clearAllMarks(player.getUniqueId());
    }
}