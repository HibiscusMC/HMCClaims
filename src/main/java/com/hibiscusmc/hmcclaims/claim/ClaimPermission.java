package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import lombok.Getter;

/**
 * Represents individual permissions that can be granted to either a
 * {@link ClaimMember} or a {@link ClaimRole} within a claim.
 *
 * <p>Permissions control what actions a member is allowed to perform
 * inside the claim boundaries.</p>
 */
@Getter
public enum ClaimPermission {
    PLACE_BLOCK("Place Blocks", "Allows placing blocks within the claim."),
    BREAK_BLOCK("Break Blocks", "Allows breaking blocks within the claim."),
    INTERACT_BLOCK("Block Interaction", "Allows interacting with blocks such as doors and buttons."),
    INTERACT_ENTITY("Entity Interaction", "Allows interacting with entities such as villagers or animals."),
    DAMAGE_ENTITY("Entity Damage", "Allows damaging entities within the claim."),
    USE_CONTAINER("Use Containers", "Allows opening and using containers such as chests and furnaces."),
    USE_ITEM("Use Items", "Allows using items such as buckets or flint and steel."),
    PICKUP_ITEMS("Item Pickup", "Allows picking up dropped items within the claim."),
    DROP_ITEMS("Item Drop", "Allows dropping items within the claim.");

    private final String displayName;
    private final String description;

    ClaimPermission(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}