package com.hibiscusmc.hmcclaims.api.event;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ClaimResizeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @NotNull
    private final Player player;

    @NotNull
    private final Claim claim;

    @NotNull
    private final ClaimRegion oldRegion;

    @NotNull
    private final ClaimRegion newRegion;

    public ClaimResizeEvent(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimRegion oldRegion, @NotNull ClaimRegion newRegion) {
        super(true);
        this.player = player;
        this.claim = claim;
        this.oldRegion = oldRegion;
        this.newRegion = newRegion;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}