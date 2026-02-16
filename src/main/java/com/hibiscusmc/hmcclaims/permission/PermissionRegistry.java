package com.hibiscusmc.hmcclaims.permission;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import team.unnamed.inject.Singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class PermissionRegistry {

    private final static Map<String, Permission> PERMISSIONS = new HashMap<>();

    public void load() {
        register(Permission.PLACE_BLOCK);
        register(Permission.BREAK_BLOCK);
        register(Permission.INTERACT_BLOCK);
        register(Permission.INTERACT_ENTITY);
        register(Permission.DAMAGE_ENTITY);
        register(Permission.USE_CONTAINER);
        register(Permission.USE_ITEM);
        register(Permission.PICKUP_ITEM);
        register(Permission.DROP_ITEM);
    }

    public void register(Permission permission) {
        String key = permission.key().asString();

        if (PERMISSIONS.containsKey(key)) {
            throw new IllegalArgumentException("Permission already registered: " + permission.key().asString());
        }

        PERMISSIONS.put(key, permission);
    }

    public Permission getPermission(String key) {
        return getPermission(RegistryUtil.withKey(key));
    }

    public Permission getPermission(Key key) {
        return PERMISSIONS.get(key.asString());
    }

    public static Collection<Permission> getAllPermissions() {
        return Collections.unmodifiableCollection(PERMISSIONS.values());
    }

}