package com.hibiscusmc.hmcclaims.listener.setting;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.DefaultSettings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import team.unnamed.inject.Inject;

import java.util.Iterator;
import java.util.List;

/**
 * Mob & Block Explosions, Entity Trampling
 */
public class VanillaBlockSettingListener implements Listener {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<DefaultSettings> defaultSettingsHolder;

    @EventHandler
    public void onMobExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList(), switch (event.getEntity()) {
            case TNTPrimed ignore -> Setting.BLOCK_EXPLOSIONS;
            case ExplosiveMinecart ignore -> Setting.BLOCK_EXPLOSIONS;
            default -> Setting.MOB_EXPLOSIONS;
        });
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList(), Setting.BLOCK_EXPLOSIONS);
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

    private boolean isEnabled(Claim claim, Setting<Boolean> setting) {
        SettingHolder<Boolean> holder = (SettingHolder<Boolean>) claim.settings().get(setting);
        Boolean value = holder != null ? holder.value() : null;

        if (value != null) {
            return value;
        }

        String rawValue = defaultSettingsHolder.get().defaultSettings().get(setting);

        return rawValue != null ? setting.parser().apply(rawValue) : setting.defaultValue();
    }

    private void handleExplosion(List<Block> blockList, Setting<Boolean> setting) {
        Iterator<Block> it = blockList.iterator();

        while (it.hasNext()) {
            Block block = it.next();
            Claim claim = claimManager.getClaimAt(block.getLocation())
                    .orElse(null);

            if (claim == null) {
                continue;
            }

            SettingHolder<Boolean> holder = (SettingHolder<Boolean>) claim.settings().get(setting);
            if (holder != null && Boolean.TRUE.equals(holder.value())) {
                continue;
            }

            it.remove();
        }
    }
}