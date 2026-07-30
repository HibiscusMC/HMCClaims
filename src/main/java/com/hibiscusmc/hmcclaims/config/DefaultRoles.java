package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@ConfigSerializable
public class DefaultRoles {

    @Setting("default-roles")
    private List<ClaimRole> defaultRoles = List.of(
            new ClaimRole(null, "Owner", new HashSet<>(PermissionRegistry.getAllPermissions())),
            new ClaimRole(null, "Member", Set.of(
                    Permission.PLACE_BLOCK,
                    Permission.BREAK_BLOCK,
                    Permission.USE_CONTAINER,
                    Permission.USE_ITEM,
                    Permission.PICKUP_ITEM,
                    Permission.DROP_ITEM,
                    Permission.DAMAGE_ENTITY,
                    Permission.INTERACT_ENTITY
            )),
            new ClaimRole(null, "Everyone", Set.of(
                    Permission.USE_ITEM,
                    Permission.DROP_ITEM,
                    Permission.PICKUP_ITEM
            ))
    );

}