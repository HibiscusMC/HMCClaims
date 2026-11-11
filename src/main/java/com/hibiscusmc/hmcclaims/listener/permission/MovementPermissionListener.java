package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fly, Elytra, Locked Claim Entry
 */
@Singleton
public class MovementPermissionListener implements Listener {

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    private final Map<UUID, Location> lastRidingLocations
            = new HashMap<>();

    @Inject
    public MovementPermissionListener(SchedulerUtil scheduler) {
        scheduler.scheduleTimer(this::checkPassengers, 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLockedClaimEnter(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Claim claim = claimManager.getClaimAt(to).orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.equals(claimManager.getClaimAt(from).orElse(null))) {
            return;
        }

        Player player = event.getPlayer();
        String denial = entryDenial(player, claim);

        if (denial == null) {
            return;
        }

        text.sendNotification(player, denial);

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }

        event.setTo(from);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLockedClaimTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();

        Claim claim = claimManager.getClaimAt(to).orElse(null);

        if (claim == null) {
            return;
        }

        Player player = event.getPlayer();
        String denial = entryDenial(player, claim);

        if (denial == null) {
            return;
        }

        text.sendNotification(player, denial);
        event.setCancelled(true);
    }

    // Passengers that aren't steering get no PlayerMoveEvent, so riders are checked on a timer instead.
    private void checkPassengers() {
        lastRidingLocations.keySet().removeIf(playerId -> {
            Player player = Bukkit.getPlayer(playerId);

            return player == null || !player.isInsideVehicle();
        });

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isInsideVehicle()) {
                continue;
            }

            Location location = player.getLocation();
            Location last = lastRidingLocations.put(player.getUniqueId(), location);

            if (last == null || !location.getWorld().equals(last.getWorld())) {
                continue;
            }

            Claim claim = claimManager.getClaimAt(location).orElse(null);

            if (claim == null || claim.equals(claimManager.getClaimAt(last).orElse(null))) {
                continue;
            }

            String denial = entryDenial(player, claim);

            if (denial == null) {
                continue;
            }

            text.sendNotification(player, denial);

            lastRidingLocations.remove(player.getUniqueId());
            player.leaveVehicle();
            player.teleport(last, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    private @Nullable String entryDenial(@NotNull Player player, @NotNull Claim claim) {
        ClaimAccess.EntryDenial denial = access.entryDenial(player, claim);

        if (denial == null) {
            return null;
        }

        return switch (denial) {
            case BANNED -> messagesHolder.get().claims().permissions().memberBanned();
            case LOCKED -> messagesHolder.get().claims().permissions().ignoreLocked();
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.ALLOW_FLIGHT)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().allowFlight());
        event.setCancelled(true);
        player.setFlying(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFlyInClaim(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location to = event.getTo();

        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Claim claim = claimManager.getClaimAt(to).orElse(null);
        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.ALLOW_FLIGHT)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().allowFlight());
        player.setFlying(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGlideIntoClaim(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }

        Location to = event.getTo();

        Claim fromClaim = claimManager.getClaimAt(event.getFrom()).orElse(null);
        Claim toClaim = claimManager.getClaimAt(to).orElse(null);
        if (toClaim == null || toClaim.equals(fromClaim)) {
            return;
        }

        if (access.hasPermission(player, toClaim, Permission.USE_ELYTRA)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useElytra());
        player.setGliding(false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onElytra(EntityToggleGlideEvent event) {
        if (!event.isGliding()) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_ELYTRA)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useElytra());
        event.setCancelled(true);
    }
}