package com.hibiscusmc.hmcclaims.listener.permission;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import team.unnamed.inject.Inject;

/**
 * Place Blocks, Break Blocks, Use Containers, Doors, Trapdoors, Redstone,
 * Player Interact, Ignite, Harvest, Plant, Trample Soil
 */
public class VanillaBlockListener implements Listener {

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

        BlockState state = block.getState();
        if (!(state instanceof Container)) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_CONTAINER)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().interactBlock());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_DOOR)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().useDoor());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_TRAPDOOR)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().useTrapdoor());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.USE_REDSTONE)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().useRedstone());
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
        if (block.getState() instanceof Container
                || isDoor(type)
                || Tag.TRAPDOORS.isTagged(type)
                || isRedstone(type, event.getAction())) {
            return;
        }

        // Only cancel when the block actually has an interaction
        var blockType = type.asBlockType();
        if (blockType == null || !blockType.isInteractable()) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.PLAYER_INTERACT)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().playerInteract());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.IGNITE_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().igniteBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgniteBlock(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.IGNITE_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().igniteBlock());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (isPlantable(block.getType())) {
            if (claim.hasPermission(player.getUniqueId(), Permission.PLANT_CROPS)
                    || claim.hasPermission(player.getUniqueId(), Permission.PLACE_BLOCK)) {
                return;
            }

            text.send(player, messagesHolder.get().claims().permissions().plantCrops());
            event.setCancelled(true);
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.PLACE_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().placeBlock());
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

        if (isHarvestable(block)) {
            if (claim.hasPermission(player.getUniqueId(), Permission.HARVEST_CROPS)
                    || claim.hasPermission(player.getUniqueId(), Permission.BREAK_BLOCK)) {
                return;
            }

            text.send(player, messagesHolder.get().claims().permissions().harvestCrops());
            event.setCancelled(true);
            return;
        }

        if (claim.hasPermission(player.getUniqueId(), Permission.BREAK_BLOCK)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().breakBlock());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.TRAMPLE_SOIL)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().trampleSoil());
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

        if (claim.hasPermission(player.getUniqueId(), Permission.TRAMPLE_SOIL)) {
            return;
        }

        text.send(player, messagesHolder.get().claims().permissions().trampleSoil());
        event.setCancelled(true);
    }

    private static boolean isDoor(Material type) {
        return Tag.DOORS.isTagged(type) || Tag.FENCE_GATES.isTagged(type);
    }

    private static boolean isRedstone(Material type, Action action) {
        if (Tag.PRESSURE_PLATES.isTagged(type)) {
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

    private static boolean isPlantable(Material type) {
        return Tag.CROPS.isTagged(type)
                || Tag.SAPLINGS.isTagged(type)
                || type == Material.MELON_STEM
                || type == Material.PUMPKIN_STEM
                || type == Material.ATTACHED_MELON_STEM
                || type == Material.ATTACHED_PUMPKIN_STEM
                || type == Material.SWEET_BERRY_BUSH
                || type == Material.CAVE_VINES
                || type == Material.CAVE_VINES_PLANT
                || type == Material.NETHER_WART
                || type == Material.COCOA
                || type == Material.CHORUS_FLOWER;
    }

    private static boolean isHarvestable(Block block) {
        Material type = block.getType();
        if (Tag.CROPS.isTagged(type)
                || type == Material.NETHER_WART
                || type == Material.COCOA
                || type == Material.SWEET_BERRY_BUSH
                || type == Material.CAVE_VINES
                || type == Material.CAVE_VINES_PLANT) {
            return block.getBlockData() instanceof Ageable;
        }

        return type == Material.MELON
                || type == Material.PUMPKIN
                || type == Material.SUGAR_CANE
                || type == Material.CACTUS
                || type == Material.BAMBOO
                || type == Material.KELP
                || type == Material.KELP_PLANT;
    }
}
