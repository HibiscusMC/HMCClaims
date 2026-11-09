package com.hibiscusmc.hmcclaims.listener.protection;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

/**
 * Pistons, Liquid Flow, Fire Spread, Dispensers, Structure Growth, Mob Griefing and
 * Falling Blocks crossing into claims
 */
public class BorderProtectionListener implements Listener {

    @Inject
    private ClaimManager claimManager;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Location source = event.getBlock().getLocation();
        BlockFace direction = event.getDirection();

        if (!canAffect(source, event.getBlock().getRelative(direction).getLocation())) {
            event.setCancelled(true);
            return;
        }

        for (Block block : event.getBlocks()) {
            if (!canAffect(source, block.getLocation())
                    || !canAffect(source, block.getRelative(direction).getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Location source = event.getBlock().getLocation();
        BlockFace direction = event.getDirection();

        for (Block block : event.getBlocks()) {
            if (!canAffect(source, block.getLocation())
                    || !canAffect(source, block.getRelative(direction).getLocation())
                    || !canAffect(source, block.getRelative(direction.getOppositeFace()).getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!canAffect(event.getBlock().getLocation(), event.getToBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Material type = event.getSource().getType();
        if (type != Material.FIRE && type != Material.SOUL_FIRE) {
            return;
        }

        if (!canAffect(event.getSource().getLocation(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block igniting = event.getIgnitingBlock();
        if (igniting == null) {
            return;
        }

        if (!canAffect(igniting.getLocation(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        if (cause != IgniteCause.SPREAD && cause != IgniteCause.LAVA) {
            return;
        }

        Block igniting = event.getIgnitingBlock();
        if (igniting == null) {
            return;
        }

        if (!canAffect(igniting.getLocation(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Directional directional)) {
            return;
        }

        Block target = event.getBlock().getRelative(directional.getFacing());

        if (!canAffect(event.getBlock().getLocation(), target.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Location source = event.getLocation();

        event.getBlocks().removeIf(state -> !canAffect(source, state.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        Location source = event.getBlock().getLocation();

        event.getBlocks().removeIf(state -> !canAffect(source, state.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof FallingBlock) {
            Location origin = entity.getOrigin();
            if (origin == null) {
                return;
            }

            if (!canAffect(origin, event.getBlock().getLocation())) {
                event.setCancelled(true);
            }

            return;
        }

        if (!isGriefer(entity)) {
            return;
        }

        if (claimManager.getClaimAt(event.getBlock().getLocation()).isPresent()) {
            event.setCancelled(true);
        }
    }

    private boolean canAffect(@NotNull Location source, @NotNull Location target) {
        Claim targetClaim = claimManager.getClaimAt(target).orElse(null);
        if (targetClaim == null) {
            return true;
        }

        Claim sourceClaim = claimManager.getClaimAt(source).orElse(null);

        return sourceClaim != null && sourceClaim.owner().equals(targetClaim.owner());
    }

    private static boolean isGriefer(Entity entity) {
        return entity instanceof Enderman
                || entity instanceof Wither
                || entity instanceof EnderDragon
                || entity instanceof Ravager
                || entity instanceof Silverfish
                || entity instanceof Zombie;
    }
}
