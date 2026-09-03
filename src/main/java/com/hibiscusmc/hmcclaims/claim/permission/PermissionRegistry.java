package com.hibiscusmc.hmcclaims.claim.permission;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A centralized registry for all valid claim permissions.
 */
public class PermissionRegistry {

    /**
     * Internal map storing permissions indexed by their unique key string.
     */
    private final static Map<String, Permission> PERMISSIONS = new HashMap<>();

    /**
     * Private constructor to prevent instantiation of a utility-based registry.
     */
    private PermissionRegistry() {
    }

    static {
        // Claim-related permissions
        register(Permission.MANAGE_MEMBERS);
        register(Permission.BAN_MEMBERS);
        register(Permission.MANAGE_MEMBER_ROLES);
        register(Permission.MANAGE_MEMBER_PERMISSIONS);
        register(Permission.MANAGE_ROLES);
        register(Permission.MANAGE_ROLE_PERMISSIONS);

        // Interactions within the claim permissions
        register(Permission.PLACE_BLOCK);
        register(Permission.BREAK_BLOCK);
        register(Permission.USE_CONTAINER);
        register(Permission.INTERACT_ENTITY);
        register(Permission.DAMAGE_ENTITY);
        register(Permission.USE_ITEM);
        register(Permission.PICKUP_ITEM);
        register(Permission.DROP_ITEM);

        register(Permission.IGNITE_BLOCK);
        register(Permission.PLAYER_INTERACT);
        register(Permission.USE_REDSTONE);
        register(Permission.USE_DOOR);
        register(Permission.USE_TRAPDOOR);
        register(Permission.USE_LECTERN);
        register(Permission.ALLOW_FLIGHT);
        register(Permission.USE_ELYTRA);
        register(Permission.IGNORE_LOCKED);
        register(Permission.USE_VEHICLE);
        register(Permission.TRAMPLE_SOIL);
        register(Permission.HARVEST_CROPS);
        register(Permission.PLANT_CROPS);
        register(Permission.USE_WIND_CHARGE);
    }

    /**
     * Registers a new permission node into the system.
     *
     * @param permission The permission instance to register.
     * @throws IllegalArgumentException If a permission with the same key is already registered.
     */
    public static void register(Permission permission) {
        String key = permission.key().asString();

        if (PERMISSIONS.containsKey(key)) {
            throw new IllegalArgumentException("Permission already registered: " + permission.key().asString());
        }

        PERMISSIONS.put(key, permission);
    }

    /**
     * Retrieves a permission by its string key.
     *
     * @param key The key string (e.g., "hmcclaims:break_block").
     * @return The {@link Permission} instance, or {@code null} if not found.
     */
    @Nullable
    @Contract(pure = true)
    public static Permission getPermission(String key) {
        return getPermission(RegistryUtil.key(key));
    }

    /**
     * Retrieves a permission by its namespaced Key.
     *
     * @param key The {@link Key} to look up.
     * @return The {@link Permission} instance, or {@code null} if not found.
     */
    @Nullable
    @Contract(pure = true)
    public static Permission getPermission(Key key) {
        return PERMISSIONS.get(key.asString());
    }

    /**
     * Gets a read-only collection of all currently registered permissions.
     *
     * @return An unmodifiable view of the registered permissions.
     */
    @NotNull
    @Contract(pure = true)
    public static Collection<Permission> getAllPermissions() {
        return Collections.unmodifiableCollection(PERMISSIONS.values());
    }
}