package com.hibiscusmc.hmcclaims.api.event;

import com.hibiscusmc.hmcclaims.claim.Claim;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called after something changed who can do what in a claim.
 */
@Getter
public class ClaimAccessChangeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @NotNull
    private final Claim claim;

    @NotNull
    private final Cause cause;

    /**
     * The affected member, or null when the change affects many players.
     */
    @Nullable
    private final UUID memberId;

    public ClaimAccessChangeEvent(@NotNull Claim claim, @NotNull Cause cause, @Nullable UUID memberId) {
        super(!Bukkit.isPrimaryThread());
        this.claim = claim;
        this.cause = cause;
        this.memberId = memberId;
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

    public enum Cause {
        MEMBER_ADDED,
        MEMBER_REMOVED,
        MEMBER_BANNED,
        MEMBER_ROLE_CHANGED,
        MEMBER_PERMISSIONS_CHANGED,
        ROLE_PERMISSIONS_CHANGED,
        ROLE_DELETED,
        OWNER_TRANSFERRED,
        PERMISSIONS_INHERITED
    }
}
