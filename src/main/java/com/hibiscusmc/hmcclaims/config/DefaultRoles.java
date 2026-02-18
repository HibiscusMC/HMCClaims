package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.permission.Permission;
import com.hibiscusmc.hmcclaims.permission.PermissionRegistry;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@ToString
@ConfigSerializable
public class DefaultRoles {

    @Setting("default-roles")
    private List<ClaimRole> defaultRoles = List.of(
            new ClaimRole("owner", "Owner", new HashSet<>(PermissionRegistry.getAllPermissions())),
            new ClaimRole("member", "Member", Set.of(
                    Permission.USE_ITEM,
                    Permission.USE_CONTAINER,
                    Permission.PICKUP_ITEM,
                    Permission.DROP_ITEM,
                    Permission.PLACE_BLOCK,
                    Permission.BREAK_BLOCK,
                    Permission.DAMAGE_ENTITY,
                    Permission.INTERACT_BLOCK,
                    Permission.INTERACT_ENTITY
            )),
            new ClaimRole("everyone", "Everyone", Set.of(
                    Permission.USE_ITEM,
                    Permission.DROP_ITEM,
                    Permission.PICKUP_ITEM
            ))
    );

}