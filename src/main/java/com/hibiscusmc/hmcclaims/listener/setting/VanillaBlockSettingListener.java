package com.hibiscusmc.hmcclaims.listener.setting;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

import java.util.List;

/**
 * Mob & Block Explosions (including their damage to hanging entities and entities), Entity Trampling.
 * Explosions starting outside a claim never affect it.
 */
public class VanillaBlockSettingListener implements Listener {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<ClaimSettings> claimSettingsHolder;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMobExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof AbstractWindCharge) {
            return;
        }

        Entity entity = event.getEntity();

        handleExplosion(event.blockList(), settingFor(entity), explosionSource(entity, event.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList(), Setting.BLOCK_EXPLOSIONS, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingExplode(HangingBreakEvent event) {
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }

        Entity remover = event instanceof HangingBreakByEntityEvent byEntity ? byEntity.getRemover() : null;
        Location target = event.getEntity().getLocation();

        if (!allowsExplosion(explosionSource(remover, target), target, settingFor(remover))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            return;
        }

        Entity damager = event.getDamager();
        Location target = event.getEntity().getLocation();

        if (!allowsExplosion(explosionSource(damager, target), target, settingFor(damager))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTrampleSoil(EntityInteractEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.FARMLAND) {
            return;
        }

        List<Entity> passengers = entity.getPassengers();
        if (!passengers.isEmpty() && passengers.getFirst() instanceof Player) {
            return;
        }

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (isEnabled(claim, Setting.ENTITY_TRAMPLING)) {
            return;
        }

        event.setCancelled(true);
    }

    private static Setting<Boolean> settingFor(@Nullable Entity entity) {
        return switch (entity) {
            case null -> Setting.BLOCK_EXPLOSIONS;
            case TNTPrimed ignore -> Setting.BLOCK_EXPLOSIONS;
            case ExplosiveMinecart ignore -> Setting.BLOCK_EXPLOSIONS;
            default -> Setting.MOB_EXPLOSIONS;
        };
    }

    private static Location explosionSource(@Nullable Entity entity, @NotNull Location fallback) {
        if (entity == null || entity instanceof LivingEntity) {
            return fallback;
        }

        Location origin = entity.getOrigin();

        return origin != null ? origin : fallback;
    }

    private boolean allowsExplosion(@NotNull Location source, @NotNull Location target, @NotNull Setting<Boolean> setting) {
        Claim claim = claimManager.getClaimAt(target).orElse(null);
        if (claim == null) {
            return true;
        }

        if (!claimManager.canAffect(source, target)) {
            return false;
        }

        return isEnabled(claim, setting);
    }

    private boolean isEnabled(Claim claim, Setting<Boolean> setting) {
        SettingHolder<Boolean> holder = (SettingHolder<Boolean>) claim.settings().get(setting);
        Boolean value = holder != null ? holder.value() : null;

        if (value != null) {
            return value;
        }

        Boolean configured = claimSettingsHolder.get().defaultValue(setting);

        return configured != null ? configured : setting.defaultValue();
    }

    private void handleExplosion(List<Block> blockList, Setting<Boolean> setting, Location source) {
        blockList.removeIf(block -> !allowsExplosion(source, block.getLocation(), setting));
    }
}
