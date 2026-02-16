package com.hibiscusmc.hmcclaims.permission;

import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;

/**
 * Represents individual permissions that can be granted to either a
 * {@link ClaimMember} or a {@link ClaimRole} within a claim.
 *
 * <p>Permissions control what actions a member is allowed to perform
 * inside the claim boundaries.</p>
 */
public record Permission(Key key, String displayName, String description) {

    public Permission(String id, String displayName, String description) {
        this(RegistryUtil.withKey(id), displayName, description);
    }

    public final static Permission PLACE_BLOCK =
            new Permission("place_block", "Place Blocks", "Allows placing blocks within the claim.");
    public final static Permission BREAK_BLOCK =
            new Permission("break_block", "Break Blocks", "Allows breaking blocks within the claim.");
    public final static Permission INTERACT_BLOCK =
            new Permission("interact_block", "Block Interaction", "Allows interacting with blocks such as doors and buttons.");
    public final static Permission INTERACT_ENTITY =
            new Permission("interact_entity", "Entity Interaction", "Allows interacting with entities such as villagers or animals.");
    public final static Permission DAMAGE_ENTITY =
            new Permission("damage_entity", "Entity Damage", "Allows damaging entities within the claim.");
    public final static Permission USE_CONTAINER =
            new Permission("use_container", "Use Containers", "Allows opening and using containers such as chests and furnaces.");
    public final static Permission USE_ITEM =
            new Permission("use_item", "Use Items", "Allows using items such as buckets or flint and steel.");
    public final static Permission PICKUP_ITEM =
            new Permission("pickup_item", "Item Pickup", "Allows picking up dropped items within the claim.");
    public final static Permission DROP_ITEM =
            new Permission("drop_item", "Item Drop", "Allows dropping items within the claim.");

}