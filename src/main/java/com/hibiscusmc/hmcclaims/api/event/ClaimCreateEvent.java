package com.hibiscusmc.hmcclaims.api.event;

import com.hibiscusmc.hmcclaims.claim.Claim;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class ClaimCreateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @NotNull
    private final Player player;

    @NotNull
    private final Claim claim;

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