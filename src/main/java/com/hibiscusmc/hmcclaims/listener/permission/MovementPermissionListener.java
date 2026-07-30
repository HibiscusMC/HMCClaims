package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
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
import team.unnamed.inject.Inject;

/**
 * Fly, Elytra, Locked Claim Entry
 */
public class MovementPermissionListener implements Listener {

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLockedClaimEnter(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        Claim fromClaim = claimManager.getClaimAt(from).orElse(null);
        Claim toClaim = claimManager.getClaimAt(to).orElse(null);

        if (toClaim == null || toClaim.equals(fromClaim) || !toClaim.locked()) {
            return;
        }

        Player player = event.getPlayer();
        if (toClaim.hasPermission(player.getUniqueId(), Permission.IGNORE_LOCKED)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().ignoreLocked());
        event.setTo(from);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLockedClaimTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Claim fromClaim = claimManager.getClaimAt(event.getFrom()).orElse(null);
        Claim toClaim = claimManager.getClaimAt(to).orElse(null);

        if (toClaim == null || toClaim.equals(fromClaim) || !toClaim.locked()) {
            return;
        }

        Player player = event.getPlayer();
        if (toClaim.hasPermission(player.getUniqueId(), Permission.IGNORE_LOCKED)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().ignoreLocked());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(player.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.ALLOW_FLIGHT)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().allowFlight());
        event.setCancelled(true);
        player.setFlying(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFlyInClaim(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Claim claim = claimManager.getClaimAt(to).orElse(null);
        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.ALLOW_FLIGHT)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().allowFlight());
        player.setFlying(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGlideIntoClaim(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Claim fromClaim = claimManager.getClaimAt(event.getFrom()).orElse(null);
        Claim toClaim = claimManager.getClaimAt(to).orElse(null);
        if (toClaim == null || toClaim.equals(fromClaim)) {
            return;
        }

        if (toClaim.hasPermission(player.getUniqueId(), Permission.USE_ELYTRA)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().useElytra());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_ELYTRA)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().useElytra());
        event.setCancelled(true);
    }
}
