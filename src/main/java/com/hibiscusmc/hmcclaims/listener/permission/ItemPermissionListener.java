package com.hibiscusmc.hmcclaims.listener.permission;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.util.EntityUtil;
import com.hibiscusmc.hmcclaims.util.PlantUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

/**
 * Entity Interaction, Container Vehicles, Item Use, Item Pickup and Drop, Wind Burst, Wind Charge Explosions, Buckets, Vehicle Use
 */
public class ItemPermissionListener implements Listener {

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(event.getRightClicked().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.INTERACT_ENTITY)) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            text.sendNotification(player, messagesHolder.get().claims().permissions().interactEntity());
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntityClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Player) {
            return;
        }

        Permission permission;
        String message;

        if (entity instanceof Vehicle && !(entity instanceof LivingEntity)) {
            if (!(entity instanceof InventoryHolder)) {
                return;
            }

            permission = Permission.USE_CONTAINER;
            message = messagesHolder.get().claims().permissions().interactBlock();
        } else {
            permission = Permission.INTERACT_ENTITY;
            message = messagesHolder.get().claims().permissions().interactEntity();
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(entity.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, permission)) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            text.sendNotification(player, message);
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseItem(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir() || isHandledBySpecificPermission(item.getType())) {
            return;
        }

        Claim claim = claimManager.getClaimAt(
                        event.getClickedBlock() != null
                                ? event.getClickedBlock().getLocation()
                                : event.getPlayer().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        Player player = event.getPlayer();
        if (access.hasPermission(player, claim, Permission.USE_ITEM)) {
            return;
        }

        if (PlantUtil.isPlantableItem(item.getType())
                && (access.hasPermission(player, claim, Permission.PLANT_CROPS)
                || access.hasPermission(player, claim, Permission.PLACE_BLOCK))) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            text.sendNotification(player, messagesHolder.get().claims().permissions().useItem());
        }
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getItem().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.PICKUP_ITEM)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().pickupItem());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(event.getItemDrop().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.DROP_ITEM)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().dropItem());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWindCharge(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof AbstractWindCharge)) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(player.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_WIND_CHARGE)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useWindCharge());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMaceWindCharge(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasEnchant(Enchantment.WIND_BURST)) {
            return;
        }

        // Wind Burst only triggers from a smash attack (falling).
        if (player.getFallDistance() <= 1.5f) {
            return;
        }

        Claim claim = claimManager.getClaimAt(event.getEntity().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_WIND_CHARGE)) {
            return;
        }

        // Paper does not expose a cancellable event for only the Wind Burst
        // effect, so denying it also denies the triggering smash attack.
        text.sendNotification(player, messagesHolder.get().claims().permissions().useWindCharge());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();
        Claim claim = claimManager.getClaimAt(vehicle.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_VEHICLE)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useVehicle());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehiclePlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Vehicle)) {
            return;
        }

        Claim claim = claimManager.getClaimAt(entity.getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_VEHICLE)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useVehicle());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWindChargeExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof AbstractWindCharge charge)) {
            return;
        }

        Player player = EntityUtil.responsiblePlayer(charge);

        event.blockList().removeIf(block -> !canWindCharge(player, block));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_ITEM)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useItem());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaimAt(event.getBlock().getLocation())
                .orElse(null);

        if (claim == null) {
            return;
        }

        if (access.hasPermission(player, claim, Permission.USE_ITEM)) {
            return;
        }

        text.sendNotification(player, messagesHolder.get().claims().permissions().useItem());
        event.setCancelled(true);
    }

    private boolean canWindCharge(Player player, Block block) {
        Claim claim = claimManager.getClaimAt(block.getLocation())
                .orElse(null);

        if (claim == null) {
            return true;
        }

        return player != null
                && access.hasPermission(player, claim, Permission.USE_WIND_CHARGE);
    }

    private static boolean isHandledBySpecificPermission(Material type) {
        String name = type.name();
        return type.asBlockType() != null
                || type == Material.FLINT_AND_STEEL
                || type == Material.FIRE_CHARGE
                || type == Material.WIND_CHARGE
                || name.endsWith("_BOAT")
                || name.endsWith("_RAFT")
                || name.endsWith("_MINECART");
    }
}