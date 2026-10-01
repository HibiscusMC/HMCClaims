package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class Permissions {

    @Comment("Permissions listed here still work but aren't shown in menus.")
    private List<String> hidden = new ArrayList<>();

    @Comment("The roles every new claim starts with. Use '*' to grant every permission.")
    private Roles roles = new Roles();

    @Comment("How every permission is displayed in menus. New permissions are added automatically.")
    private Map<Permission, Entry> permissions = defaults();

    private static Map<Permission, Entry> defaults() {
        Map<Permission, Entry> defaults = new LinkedHashMap<>();

        for (Permission permission : PermissionRegistry.getAllPermissions()) {
            defaults.put(permission, Entry.of(permission));
        }

        return defaults;
    }

    /**
     * Returns the permissions that should be shown in menus, in registration order.
     */
    @NotNull
    public List<Permission> visible() {
        return PermissionRegistry.getAllPermissions().stream()
                .filter(permission -> !isHidden(permission))
                .toList();
    }

    public boolean isHidden(@NotNull Permission permission) {
        String id = RegistryUtil.serialize(permission.key());

        return hidden.stream().anyMatch(value -> value.equals(id) || value.equals(permission.key().asString()));
    }

    @NotNull
    public Entry entry(@NotNull Permission permission) {
        Entry entry = permissions.get(permission);

        return entry != null ? entry : Entry.of(permission);
    }

    @Getter
    @Section
    @SuppressWarnings({"FieldMayBeFinal"})
    public static class Roles {

        private DefaultRole owner = new DefaultRole("Owner", "*");

        private DefaultRole member = new DefaultRole("Member", ids(
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
        ));

        private DefaultRole everyone = new DefaultRole("Everyone", ids(
                Permission.USE_ITEM,
                Permission.DROP_ITEM,
                Permission.PICKUP_ITEM,
                Permission.USE_LECTERN
        ));

        private static String[] ids(Permission... permissions) {
            return Arrays.stream(permissions)
                    .map(permission -> RegistryUtil.serialize(permission.key()))
                    .toArray(String[]::new);
        }
    }

    @Getter
    @Section
    @SuppressWarnings({"FieldMayBeFinal"})
    public static class DefaultRole {

        private String name = "";

        private List<String> permissions = new ArrayList<>();

        public DefaultRole() {
        }

        public DefaultRole(String name, String... permissions) {
            this.name = name;
            this.permissions = new ArrayList<>(List.of(permissions));
        }
    }

    @Getter
    @Section
    @SuppressWarnings({"FieldMayBeFinal"})
    public static class Entry {

        private String name = "";

        private List<String> description = new ArrayList<>();

        private ConfigItem icon = ConfigItem.of(Material.BOOK);

        public Entry() {
        }

        @NotNull
        public static Entry of(@NotNull Permission permission) {
            Entry entry = new Entry();

            entry.name = permission.displayName();
            entry.description = Arrays.stream(("<gray>" + permission.description()).split("\n")).toList();
            entry.icon = ConfigItem.of(permission.icon());

            return entry;
        }
    }
}
