package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.Set;

@Getter
@ConfigSerializable
public class DefaultRoles {

    @Setting("default-roles")
    private List defaultRoles = new List();

    @Getter
    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal"})
    public static class List {

        @Setting(required = true)
        private ClaimRole owner = new ClaimRole(null, "Owner", new HashSet<>(PermissionRegistry.getAllPermissions()));

        @Setting(required = true)
        private ClaimRole member = new ClaimRole(null, "Member", new HashSet<>(Set.of(
                Permission.PLACE_BLOCK,
                Permission.BREAK_BLOCK,
                Permission.USE_CONTAINER,
                Permission.USE_ITEM,
                Permission.PICKUP_ITEM,
                Permission.DROP_ITEM,
                Permission.DAMAGE_ENTITY,
                Permission.INTERACT_ENTITY,
                Permission.IGNITE_BLOCK,
                Permission.PLAYER_INTERACT,
                Permission.USE_REDSTONE,
                Permission.USE_DOOR,
                Permission.USE_TRAPDOOR,
                Permission.USE_LECTERN,
                Permission.IGNORE_LOCKED,
                Permission.USE_VEHICLE,
                Permission.HARVEST_CROPS,
                Permission.PLANT_CROPS
        )));

        @Setting(required = true)
        private ClaimRole everyone = new ClaimRole(null, "Everyone", new HashSet<>(Set.of(
                Permission.USE_ITEM,
                Permission.DROP_ITEM,
                Permission.PICKUP_ITEM,
                Permission.USE_LECTERN
        )));
    }
}