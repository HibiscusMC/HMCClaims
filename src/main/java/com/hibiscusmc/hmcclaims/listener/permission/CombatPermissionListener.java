package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.util.EntityUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

/**
 * Damage Players and Entities, Break Hanging Entities, Damage Vehicles
 */
public class CombatPermissionListener implements Listener {

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ConfigHolder<ClaimSettings> claimSettingsHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        Player attacker = EntityUtil.responsiblePlayer(event.getDamager());
        if (attacker == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getEntity().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(attacker, claim, Permission.DAMAGE_ENTITY)) {
            return;
        }

        text.sendNotification(attacker, messagesHolder.get().claims().permissions().damageEntity());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = EntityUtil.responsiblePlayer(event.getDamager());
        if (attacker == null) {
            return;
        }

        if (isPvpAllowed(victim.getLocation()) && isPvpAllowed(attacker.getLocation())) {
            return;
        }

        text.sendNotification(attacker, messagesHolder.get().claims().settings().pvpDisabled());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Player player = EntityUtil.responsiblePlayer(event.getRemover());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getEntity().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.BREAK_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().breakBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Player player = EntityUtil.responsiblePlayer(event.getAttacker());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getVehicle().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.DAMAGE_ENTITY)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().damageEntity());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Player player = EntityUtil.responsiblePlayer(event.getAttacker());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getVehicle().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.DAMAGE_ENTITY)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().damageEntity());
        event.setCancelled(true);
    }

    private boolean isPvpAllowed(@NotNull Location location) {
        Claim claim = claimManager.getClaimAt(location)
                .orElse(null);

        if (claim == null) {
            return true;
        }

        SettingHolder<Boolean> holder = (SettingHolder<Boolean>) claim.settings().get(Setting.PVP);
        Boolean value = holder != null ? holder.value() : null;

        return value != null ? value : defaultPvp();
    }

    private boolean defaultPvp() {
        Boolean value = claimSettingsHolder.get().defaultValue(Setting.PVP);

        return value != null ? value : Setting.PVP.defaultValue();
    }
}
