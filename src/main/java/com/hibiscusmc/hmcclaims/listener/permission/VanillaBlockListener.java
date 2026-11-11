package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.util.EntityUtil;
import com.hibiscusmc.hmcclaims.util.PlantUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

import java.util.List;

/**
 * Place Blocks, Break Blocks, Use Containers, Doors, Trapdoors, Lecterns,
 * Redstone, Player Interact, Ignite, TNT Priming, Projectile Block Interaction, Harvest, Plant,
 * Trample Soil, Portal Creation
 */
public class VanillaBlockListener implements Listener {

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseContainer(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!(block.getState(false) instanceof Container)) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_CONTAINER)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().interactBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseDoor(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !isDoor(block.getType())) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_DOOR)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useDoor());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseTrapdoor(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !Tag.TRAPDOORS.isTagged(block.getType())) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_TRAPDOOR)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useTrapdoor());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseLectern(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        ItemStack item = event.getItem();

        if (item != null && Tag.ITEMS_LECTERN_BOOKS.isTagged(item.getType()) && isEmptyLectern(block)) {
            if (access.hasPermission(player, claim, Permission.USE_CONTAINER)) {
                return;
            }

            text.sendNotification(player, messagesHolder.get().claims().permissions().putLecternBook());
            event.setCancelled(true);
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_LECTERN)) {
            return;
        }

        if (item != null && isAllowedPlacement(claim, player, item.getType())) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useLectern());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(event.getLectern().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_CONTAINER)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().takeLecternBook());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseRedstone(PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (block == null || !isRedstone(block.getType(), action)) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_REDSTONE)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useRedstone());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Material type = block.getType();
        if (isDoor(type)
                || Tag.TRAPDOORS.isTagged(type)
                || type == Material.LECTERN
                || isRedstone(type, event.getAction())) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.PLAYER_INTERACT)) {
            return;
        }

        if (block.getState(false) instanceof Container) {
            return;
        }

        ItemStack item = event.getItem();
        if (item != null && isAllowedPlacement(claim, player, item.getType())) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().playerInteract());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgniteBlockInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Material type = item.getType();
        if (type != Material.FLINT_AND_STEEL && type != Material.FIRE_CHARGE) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.IGNITE_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().igniteBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgniteBlock(BlockIgniteEvent event) {
        Player player = event.getPlayer() != null
                ? event.getPlayer()
                : EntityUtil.responsiblePlayer(event.getIgnitingEntity());

        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.IGNITE_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().igniteBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTntPrime(TNTPrimeEvent event) {
        Player player = EntityUtil.responsiblePlayer(event.getPrimingEntity());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.IGNITE_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().igniteBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Projectile)) {
            return;
        }

        Player player = EntityUtil.responsiblePlayer(event.getEntity());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
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
    public void onProjectileInteract(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Projectile)) {
            return;
        }

        Player player = EntityUtil.responsiblePlayer(event.getEntity());
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_REDSTONE)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useRedstone());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (event instanceof BlockMultiPlaceEvent multi && !canPlaceAll(player, multi)) {
            event.setCancelled(true);
            return;
        }

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (PlantUtil.isPlantableBlock(block.getType())) {
            if (access.hasPermission(player, claim, Permission.PLANT_CROPS)
                    || access.hasPermission(player, claim, Permission.PLACE_BLOCK)) {
                return;
            }

            text.sendNotification(player, messagesHolder.get().claims().permissions().plantCrops());
            event.setCancelled(true);
            return;
        }

        if (access.hasPermission(player, claim, Permission.PLACE_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().placeBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalCreate(PortalCreateEvent event) {
        Player player = EntityUtil.responsiblePlayer(event.getEntity());

        for (BlockState state : event.getBlocks()) {
            Claim claim = claimManager.getClaimAt(state.getLocation())
                    .orElse(null);

            if (claim == null) {
                continue;
            }

            if (player != null && access.hasPermission(player, claim, Permission.PLACE_BLOCK)) {
                continue;
            }

            if (player != null) {
                text.sendNotification(player, messagesHolder.get().claims().permissions().placeBlock());
            }

            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getEntity().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.PLACE_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().placeBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (PlantUtil.isHarvestable(block)) {
            if (access.hasPermission(player, claim, Permission.HARVEST_CROPS)
                    || access.hasPermission(player, claim, Permission.BREAK_BLOCK)) {
                return;
            }

            text.sendNotification(player, messagesHolder.get().claims().permissions().harvestCrops());
            event.setCancelled(true);
            return;
        }

        if (access.hasPermission(player, claim, Permission.BREAK_BLOCK)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().breakBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTrampleSoil(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.TRAMPLE_SOIL)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().trampleSoil());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTrampleTurtleEgg(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.TURTLE_EGG) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
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
    public void onDragonEggClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.DRAGON_EGG) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
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
    public void onEntityTrampleSoil(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getBlock().getType() != Material.FARMLAND) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.TRAMPLE_SOIL)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().trampleSoil());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRiddenTrampleSoil(EntityInteractEvent event) {
        if (event.getBlock().getType() != Material.FARMLAND) {
            return;
        }

        List<Entity> passengers = event.getEntity().getPassengers();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof Player player)) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.TRAMPLE_SOIL)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().trampleSoil());
        event.setCancelled(true);
    }

    private boolean canPlaceAll(Player player, BlockMultiPlaceEvent event) {
        boolean plantable = PlantUtil.isPlantableBlock(event.getBlockPlaced().getType());

        for (BlockState state : event.getReplacedBlockStates()) {
            Claim claim = claimManager.getClaimAt(state.getLocation())
                    .orElse(null);

            if (claim == null || access.hasPermission(player, claim, Permission.PLACE_BLOCK)) {
                continue;
            }

            if (plantable && access.hasPermission(player, claim, Permission.PLANT_CROPS)) {
                continue;
            }

            text.sendNotification(player, plantable
                    ? messagesHolder.get().claims().permissions().plantCrops()
                    : messagesHolder.get().claims().permissions().placeBlock());

            return false;
        }

        return true;
    }

    private boolean isAllowedPlacement(Claim claim, Player player, Material type) {
        if (PlantUtil.isPlantableItem(type)) {
            return access.hasPermission(player, claim, Permission.PLANT_CROPS)
                    || access.hasPermission(player, claim, Permission.PLACE_BLOCK);
        }

        return type.asBlockType() != null
                && access.hasPermission(player, claim, Permission.PLACE_BLOCK);
    }

    private static boolean isEmptyLectern(Block block) {
        return block.getBlockData() instanceof Lectern lectern && !lectern.hasBook();
    }

    private static boolean isDoor(Material type) {
        return Tag.DOORS.isTagged(type) || Tag.FENCE_GATES.isTagged(type);
    }

    private static boolean isRedstone(Material type, Action action) {
        if (Tag.PRESSURE_PLATES.isTagged(type) || type == Material.TRIPWIRE) {
            return action == Action.PHYSICAL;
        }

        if (!action.isRightClick()) {
            return false;
        }

        if (Tag.BUTTONS.isTagged(type)) {
            return true;
        }

        return switch (type) {
            case LEVER, REPEATER, COMPARATOR, DAYLIGHT_DETECTOR, NOTE_BLOCK,
                 TRIPWIRE_HOOK -> true;
            default -> false;
        };
    }
}