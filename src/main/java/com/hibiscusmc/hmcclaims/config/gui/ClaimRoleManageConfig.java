package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimRoleManageConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Permissions | <role_name> Role", 20);

    private int rows = 6;

    @Setting("screen-type")
    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<red>You're here!")
            ), 3),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 7)
    );

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete role", List.of("", "<white>Left-Click <gray>to delete role")
    ), 53);

    @Setting("cant-delete-icon")
    private SimpleIcon cantDeleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete role", List.of("", "<red>You can't delete this role")
    ), 53);

    private Map<Boolean, String> states = Map.of(
            true, "<green>Enabled",
            false, "<red>Disabled"
    );

    @Setting("permission-pages")
    private Map<Integer, List<TogglePermissionIcon<Permission>>> permissionPages = buildPermissionPages();

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @NotNull
    private Map<Integer, List<TogglePermissionIcon<Permission>>> buildPermissionPages() {
        int startSlot = 19;
        int currentPage = 1;

        List<TogglePermissionIcon<Permission>> permissions = new ArrayList<>();
        Map<Integer, List<TogglePermissionIcon<Permission>>> pages = new HashMap<>();

        for (Permission permission : PermissionRegistry.getAllPermissions()) {
            int slot = startSlot++;
            String name = permission.displayName();
            String description = permission.description();

            List<String> lore = Arrays.stream(("<gray>" + description).split("\n")).toList();

            permissions.add(new TogglePermissionIcon<>(
                    permission, slot, name, new LoreValue(lore, "<permission_value>"),
                    new ToggleIcon.BiStateToggleIcon(
                            slot + 9,
                            new PermissionIcon(ItemStack.of(Material.LIME_DYE), name, TogglePermissionIcon.buildLore(lore, "<permission_value>", true), TogglePermissionIcon.buildLore(lore, "<permission_value>", false)),
                            new PermissionIcon(ItemStack.of(Material.GRAY_DYE), name, TogglePermissionIcon.buildLore(lore, "<permission_value>", true), TogglePermissionIcon.buildLore(lore, "<permission_value>", false))
                    )
            ));

            if (startSlot > 25) {
                pages.put(currentPage, permissions);
                permissions = new ArrayList<>();

                currentPage++;
                startSlot = 19;
            }
        }

        if (!permissions.isEmpty()) {
            pages.put(currentPage, permissions);
        }

        return pages;
    }

    protected record LoreValue(List<String> lore, String value) {
    }

    @Getter
    @ConfigSerializable
    public static class PermissionIcon extends DynamicIconWithStack {

        @Setting("cant-change-lore")
        private List<String> cantChangeLore;

        protected PermissionIcon(ItemStack item, String name, List<String> lore, List<String> cantChangeLore) {
            super(item, name, lore);

            this.cantChangeLore = cantChangeLore;
        }
    }

    @Getter
    @ConfigSerializable
    public static class TogglePermissionIcon<T> extends ToggleIcon<T> {

        @Setting("no-perms-icon")
        private DynamicIconWithStack noPermsIcon;

        protected TogglePermissionIcon(T key, int slot, String name, LoreValue lore, BiStateToggleIcon modifyIcon) {
            super(key, slot, name, lore.lore(), modifyIcon);

            this.noPermsIcon = new DynamicIconWithStack(
                    ItemStack.of(Material.BOOK), name, buildLore(lore.lore(), lore.value(), false)
            );
        }

        protected static List<String> buildLore(List<String> baseLore, String value, boolean hasPermission) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "<gray>Current Value: " + value,
                    "",
                    hasPermission ? " <green><u>Click to change value" : "<red>You don't have permissions to change this"
            ));

            return cloned;
        }
    }
}