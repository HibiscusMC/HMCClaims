package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.DefaultSettings;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

/**
 * Damage Players and Entities
 */
public class CombatPermissionListener implements Listener {

    @Inject
    private ConfigHolder<Messages> messagesHolder;
    @Inject
    private ConfigHolder<DefaultSettings> defaultSettingsHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        Player attacker = getAttackingPlayer(event);
        if (attacker == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getEntity().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(attacker.getUniqueId(), Permission.DAMAGE_ENTITY)) {
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

        Player attacker = getAttackingPlayer(event);
        if (attacker == null) {
            return;
        }

        if (isPvpAllowed(victim.getLocation()) && isPvpAllowed(attacker.getLocation())) {
            return;
        }

        text.sendNotification(attacker, messagesHolder.get().claims().settings().pvpDisabled());
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
        String rawValue = defaultSettingsHolder.get().defaultSettings().get(Setting.PVP);

        return rawValue != null ? Setting.PVP.parser().apply(rawValue) : Setting.PVP.defaultValue();
    }

    private static Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }

        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }

        return null;
    }
}