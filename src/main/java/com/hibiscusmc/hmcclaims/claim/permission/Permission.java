package com.hibiscusmc.hmcclaims.claim.permission;

import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;

/**
 * Represents individual permissions that can be granted to either a
 * {@link ClaimMember} or a {@link ClaimRole} within a claim.
 */
public record Permission(Key key, String displayName, String description, Material icon) {

    public Permission(Key key, String displayName, String description) {
        this(key, displayName, description, Material.BOOK);
    }

    public Permission(String id, String displayName, String description) {
        this(RegistryUtil.key(id), displayName, description, Material.BOOK);
    }

    public Permission(String id, String displayName, String description, Material icon) {
        this(RegistryUtil.key(id), displayName, description, icon);
    }

    // Claim-related permissions
    public final static Permission MANAGE_MEMBERS =
            new Permission("manage_members", "Manage Members", "Allows to add and kick other members.", Material.PLAYER_HEAD);
    public final static Permission BAN_MEMBERS =
            new Permission("ban_members", "Ban Members", "Allows to ban other members.", Material.BARRIER);
    public final static Permission MANAGE_MEMBER_ROLES =
            new Permission("manage_member_roles", "Manage Member Roles", "Allows to modify other member roles.", Material.NAME_TAG);
    public final static Permission MANAGE_MEMBER_PERMISSIONS =
            new Permission("manage_member_permissions", "Manage Member Permissions", "Allows to modify other member permissions.", Material.WRITABLE_BOOK);
    public final static Permission MANAGE_ROLES =
            new Permission("manage_roles", "Manage Claim Roles", "Allows to create, delete and swap role positions.", Material.GOLDEN_HELMET);
    public final static Permission MANAGE_ROLE_PERMISSIONS =
            new Permission("manage_role_permissions", "Manage Role Permissions", "Allows to modify role permissions.", Material.ENCHANTED_BOOK);

    // Interactions within the claim permissions
    public final static Permission PLACE_BLOCK =
            new Permission("place_block", "Place Blocks", "Allows placing blocks within the claim.", Material.GRASS_BLOCK);
    public final static Permission BREAK_BLOCK =
            new Permission("break_block", "Break Blocks", "Allows breaking blocks within the claim.", Material.IRON_PICKAXE);
    public final static Permission INTERACT_ENTITY =
            new Permission("interact_entity", "Entity Interaction", "Allows interacting with entities such as\nvillagers or animals.", Material.LEAD);
    public final static Permission DAMAGE_ENTITY =
            new Permission("damage_entity", "Entity Damage", "Allows damaging entities within the claim.", Material.IRON_SWORD);
    public final static Permission USE_CONTAINER =
            new Permission("use_container", "Use Containers", "Allows opening and using containers\nsuch as chests and furnaces.", Material.CHEST);
    public final static Permission USE_ITEM =
            new Permission("use_item", "Use Items", "Allows using items such as buckets or flint and\nsteel.", Material.BUCKET);
    public final static Permission PICKUP_ITEM =
            new Permission("pickup_item", "Item Pickup", "Allows picking up dropped items within\nthe claim.", Material.HOPPER);
    public final static Permission DROP_ITEM =
            new Permission("drop_item", "Item Drop", "Allows dropping items within the\nclaim.", Material.DROPPER);

    public final static Permission IGNITE_BLOCK =
            new Permission("ignite_block", "Ignite Blocks", "Allows igniting blocks and placing fire\nwithin the claim.", Material.FLINT_AND_STEEL);
    public final static Permission PLAYER_INTERACT =
            new Permission("player_interact", "Player Interactions", "Allows player interactions not blocked by\nother permissions.", Material.COMPASS);
    public final static Permission USE_REDSTONE =
            new Permission("use_redstone", "Use Redstone", "Allows using redstone, levers, pressure\nplates and similar blocks.", Material.REDSTONE);
    public final static Permission USE_DOOR =
            new Permission("use_door", "Use Doors", "Allows opening and closing doors within\nthe claim.", Material.OAK_DOOR);
    public final static Permission USE_TRAPDOOR =
            new Permission("use_trapdoor", "Use Trapdoors", "Allows opening and closing trapdoors\nwithin the claim.", Material.OAK_TRAPDOOR);
    public final static Permission USE_LECTERN =
            new Permission("use_lectern", "Use Lecterns", "Allows reading books placed on lecterns\nwithin the claim.", Material.LECTERN);
    public final static Permission ALLOW_FLIGHT =
            new Permission("allow_flight", "Allow Flight", "Allows flying within the claim.", Material.FEATHER);
    public final static Permission USE_ELYTRA =
            new Permission("use_elytra", "Use Elytra", "Allows using elytras within the claim.", Material.ELYTRA);
    public final static Permission IGNORE_LOCKED =
            new Permission("ignore_locked", "Ignore Locked", "Allows entering the claim while it is locked.", Material.TRIPWIRE_HOOK);
    public final static Permission USE_VEHICLE =
            new Permission("use_vehicle", "Use Vehicles", "Allows placing and using vehicles within\nthe claim.", Material.MINECART);
    public final static Permission TRAMPLE_SOIL =
            new Permission("trample_soil", "Trample Farmland", "Allows trampling farmland within the claim.", Material.DIAMOND_HOE);
    public final static Permission HARVEST_CROPS =
            new Permission("harvest_crops", "Harvest Crops", "Allows harvesting crops within the claim.", Material.WHEAT);
    public final static Permission PLANT_CROPS =
            new Permission("plant_crops", "Plant Crops", "Allows planting crops and saplings within\nthe claim.", Material.WHEAT_SEEDS);
    public final static Permission USE_WIND_CHARGE =
            new Permission("use_wind_charge", "Use Wind Charge", "Allows using wind charges and maces with\nWind Burst within the claim.", Material.WIND_CHARGE);

}