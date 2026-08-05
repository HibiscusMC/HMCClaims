package com.hibiscusmc.hmcclaims.listener.setting;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import team.unnamed.inject.Inject;

import java.util.Iterator;
import java.util.List;

/**
 * Mob & Block Explosions
 */
public class VanillaBlockSettingListener implements Listener {

    @Inject
    private ClaimManager claimManager;

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