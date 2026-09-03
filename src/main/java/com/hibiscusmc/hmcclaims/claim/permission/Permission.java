package com.hibiscusmc.hmcclaims.claim.permission;

import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;

/**
 * Represents individual permissions that can be granted to either a
 * {@link ClaimMember} or a {@link ClaimRole} within a claim.
 */
public record Permission(Key key, String displayName, String description) {

    public Permission(String id, String displayName, String description) {
        this(RegistryUtil.key(id), displayName, description);
    }

    // Claim-related permissions
    public final static Permission MANAGE_MEMBERS =
            new Permission("manage_members", "Manage Members", "Allows to add and kick other members.");
    public final static Permission BAN_MEMBERS =
            new Permission("ban_members", "Ban Members", "Allows to ban other members.");
    public final static Permission MANAGE_MEMBER_ROLES =
            new Permission("manage_member_roles", "Manage Member Roles", "Allows to modify other member roles.");
    public final static Permission MANAGE_MEMBER_PERMISSIONS =
            new Permission("manage_member_permissions", "Manage Member Permissions", "Allows to modify other member permissions.");
    public final static Permission MANAGE_ROLES =
            new Permission("manage_roles", "Manage Claim Roles", "Allows to create, delete and swap role positions.");
    public final static Permission MANAGE_ROLE_PERMISSIONS =
            new Permission("manage_role_permissions", "Manage Role Permissions", "Allows to modify role permissions.");

    // Interactions within the claim permissions
    public final static Permission PLACE_BLOCK =
            new Permission("place_block", "Place Blocks", "Allows placing blocks within the claim.");
    public final static Permission BREAK_BLOCK =
            new Permission("break_block", "Break Blocks", "Allows breaking blocks within the claim.");
    public final static Permission INTERACT_ENTITY =
            new Permission("interact_entity", "Entity Interaction", "Allows interacting with entities such as\nvillagers or animals.");
    public final static Permission DAMAGE_ENTITY =
            new Permission("damage_entity", "Entity Damage", "Allows damaging entities within the claim.");
    public final static Permission USE_CONTAINER =
            new Permission("use_container", "Use Containers", "Allows opening and using containers\nsuch as chests and furnaces.");
    public final static Permission USE_ITEM =
            new Permission("use_item", "Use Items", "Allows using items such as buckets or flint and\nsteel.");
    public final static Permission PICKUP_ITEM =
            new Permission("pickup_item", "Item Pickup", "Allows picking up dropped items within\nthe claim.");
    public final static Permission DROP_ITEM =
            new Permission("drop_item", "Item Drop", "Allows dropping items within the\nclaim.");

    public final static Permission IGNITE_BLOCK =
            new Permission("ignite_block", "Ignite Blocks", "Allows igniting blocks and placing fire\nwithin the claim.");
    public final static Permission PLAYER_INTERACT =
            new Permission("player_interact", "Player Interactions", "Allows player interactions not blocked by\nother permissions.");
    public final static Permission USE_REDSTONE =
            new Permission("use_redstone", "Use Redstone", "Allows using redstone, levers, pressure\nplates and similar blocks.");
    public final static Permission USE_DOOR =
            new Permission("use_door", "Use Doors", "Allows opening and closing doors within\nthe claim.");
    public final static Permission USE_TRAPDOOR =
            new Permission("use_trapdoor", "Use Trapdoors", "Allows opening and closing trapdoors\nwithin the claim.");
    public final static Permission USE_LECTERN =
            new Permission("use_lectern", "Use Lecterns", "Allows reading books placed on lecterns\nwithin the claim.");
    public final static Permission ALLOW_FLIGHT =
            new Permission("allow_flight", "Allow Flight", "Allows flying within the claim.");
    public final static Permission USE_ELYTRA =
            new Permission("use_elytra", "Use Elytra", "Allows using elytras within the claim.");
    public final static Permission IGNORE_LOCKED =
            new Permission("ignore_locked", "Ignore Locked", "Allows entering the claim while it is locked.");
    public final static Permission USE_VEHICLE =
            new Permission("use_vehicle", "Use Vehicles", "Allows placing and using vehicles within\nthe claim.");
    public final static Permission TRAMPLE_SOIL =
            new Permission("trample_soil", "Trample Farmland", "Allows trampling farmland within the claim.");
    public final static Permission HARVEST_CROPS =
            new Permission("harvest_crops", "Harvest Crops", "Allows harvesting crops within the claim.");
    public final static Permission PLANT_CROPS =
            new Permission("plant_crops", "Plant Crops", "Allows planting crops and saplings within\nthe claim.");
    public final static Permission USE_WIND_CHARGE =
            new Permission("use_wind_charge", "Use Wind Charge", "Allows using wind charges and maces with\nWind Burst within the claim.");

}