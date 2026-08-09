package com.hibiscusmc.hmcclaims.api.event;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PreClaimCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter(AccessLevel.NONE)
    private boolean cancelled = false;

    @NotNull
    private final Player player;

    @NotNull
    private final ClaimRegion region;

    @Nullable
    private final Claim mainClaim;

    public PreClaimCreateEvent(@NotNull Player player, @NotNull ClaimRegion region, @Nullable Claim mainClaim) {
        super(true);
        this.player = player;
        this.region = region;
        this.mainClaim = mainClaim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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