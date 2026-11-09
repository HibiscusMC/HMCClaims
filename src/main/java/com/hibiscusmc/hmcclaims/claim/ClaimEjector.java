package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

/**
 * Sends players out of claims they're no longer allowed in. Main thread only.
 */
@Singleton
public class ClaimEjector {

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Settings> settingsHolder;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private TextUtil text;

    /**
     * Sends the player to the eject location if they're inside the claim but may no longer be there; returns whether they were sent.
     */
    public boolean ejectIfDenied(@NotNull Player player, @NotNull Claim claim) {
        if (!claim.region().contains(player.getLocation())) {
            return false;
        }

        ClaimAccess.EntryDenial denial = access.entryDenial(player, claim);

        if (denial == null) {
            return false;
        }

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }

        player.teleportAsync(ejectLocation(player), PlayerTeleportEvent.TeleportCause.PLUGIN);

        Messages.Claims messages = messagesHolder.get().claims();
        String message = switch (denial) {
            case BANNED -> messages.ejectedBanned();
            case LOCKED -> messages.ejectedLocked();
        };

        text.send(player, message, Map.of("claim", claim.name()));

        return true;
    }

    /**
     * Sends out every player inside the claim who may no longer be there.
     */
    public void ejectDenied(@NotNull Claim claim) {
        World world = claim.region().bukkitWorld();

        if (world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {
            ejectIfDenied(player, claim);
        }
    }

    private Location ejectLocation(Player player) {
        Settings.EjectLocation config = settingsHolder.get().ejectLocation();

        if (config.world().isBlank()) {
            return player.getWorld().getSpawnLocation();
        }

        World world = Bukkit.getWorld(config.world());

        if (world == null) {
            Logger.warning("The eject location's world '{}' isn't loaded, so the player's world spawn is used instead.", config.world());

            return player.getWorld().getSpawnLocation();
        }

        return new Location(world, config.x(), config.y(), config.z(), config.yaw(), config.pitch());
    }
}
