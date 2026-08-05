package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
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
public class ClaimMemberPermissionsConfig extends ClaimMemberManageConfig {

    private GuiTitle title = new GuiTitle("Manage | <member_name>", 20);

    private int rows = 6;

    @Setting("screen-type")
    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private Map<String, String> states = Map.of(
            "enabled", "<green>Enabled",
            "unset", "<gray>Unset",
            "disabled", "<red>Disabled"
    );

    @Setting("permission-pages")
    private Map<Integer, Map<String, TogglePermissionIcon<Permission>>> permissionPages = buildPermissionPages();

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Setting("kick-icon")
    private SimpleIcon kickIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Kick Member", List.of("", "<white>Left-Click <gray>to kick this member")
    ), 47);

    @Setting("cant-kick-icon")
    private ItemStack cantKickIcon = ItemUtil.build(
            Material.BARRIER, "Kick Member", List.of("", "<red>You can't kick this member")
    );

    @Setting("ban-icon")
    private SimpleIcon banIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Ban Member", List.of("", "<white>Left-Click <gray>to ban this member")
    ), 51);

    @Setting("cant-ban-icon")
    private ItemStack cantBanIcon = ItemUtil.build(
            Material.BARRIER, "Ban Member", List.of("", "<red>You can't ban this member")
    );

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleMultiIcon> tabs = Map.of(
            "roles-tab", new SimpleMultiIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), List.of(1, 2, 3)),
            "permissions-tab", new SimpleMultiIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "Permissions", List.of("", "<red>You're here!")
            ), List.of(5, 6, 7))
    );

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );

    @NotNull
    private Map<Integer, Map<String, TogglePermissionIcon<Permission>>> buildPermissionPages() {
        int startSlot = 19;
        int currentPage = 1;

        Map<String, TogglePermissionIcon<Permission>> permissions = new HashMap<>();
        Map<Integer, Map<String, TogglePermissionIcon<Permission>>> pages = new HashMap<>();

        for (Permission permission : PermissionRegistry.getAllPermissions()) {
            int slot = startSlot++;
            String name = permission.displayName();
            String description = permission.description();

            List<String> lore = Arrays.stream(("<gray>" + description).split("\n")).toList();

            permissions.put(RegistryUtil.serialize(permission.key()).replace(":", "-").toLowerCase() + "-permission", new TogglePermissionIcon<>(
                    permission, slot, name, lore,
                    new TogglePermissionIcon.TriStateToggleIcon(
                            slot + 9,
                            new PermissionIcon(ItemStack.of(Material.LIME_DYE), name, TogglePermissionIcon.buildLore(lore, true), TogglePermissionIcon.buildLore(lore, false)),
                            new PermissionIcon(ItemStack.of(Material.GRAY_DYE), name, TogglePermissionIcon.buildLore(lore, true), TogglePermissionIcon.buildLore(lore, false)),
                            new PermissionIcon(ItemStack.of(Material.RED_DYE), name, TogglePermissionIcon.buildLore(lore, true), TogglePermissionIcon.buildLore(lore, false))
                    )
            ));

            if (startSlot > 25) {
                pages.put(currentPage, permissions);
                permissions = new HashMap<>();

                currentPage++;
                startSlot = 19;
            }
        }

        if (!permissions.isEmpty()) {
            pages.put(currentPage, permissions);
        }

        return pages;
    }

    @Getter
    @ConfigSerializable
    public static class PermissionIcon extends DynamicIconWithStack {

        @Setting("cant-change-lore")
        private List<String> cantChangeLore;

        public PermissionIcon() {
        }

        protected PermissionIcon(ItemStack item, String name, List<String> lore, List<String> cantChangeLore) {
            super(item, name, lore);

            this.cantChangeLore = cantChangeLore;
        }
    }

    @Getter
    @ConfigSerializable
    public static class TogglePermissionIcon<T> {

        private int slot;

        private T key;
        private DynamicIconWithStack icon;

        @Setting("no-perms-icon")
        private DynamicIconWithStack noPermsIcon;

        @Setting("has-modify-icon")
        private boolean hasModifyIcon = true;

        @Setting("modify-icon")
        private TriStateToggleIcon modifyIcon;

        public TogglePermissionIcon() {
        }

        protected TogglePermissionIcon(T key, int slot, String name, List<String> lore, TriStateToggleIcon modifyIcon) {
            this.key = key;

            this.icon = new DynamicIconWithStack(
                    ItemStack.of(Material.BOOK), name, lore
            );
            this.slot = slot;

            this.modifyIcon = modifyIcon;

            this.noPermsIcon = new DynamicIconWithStack(
                    ItemStack.of(Material.BOOK), name, lore
            );
        }

        protected static List<String> buildLore(List<String> baseLore, boolean hasPermission) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "<gray>Current Value: <permission_value>",
                    "",
                    hasPermission ? " <green><u>Click to change value" : "<red>You don't have permissions to change this"
            ));

            return cloned;
        }

        @Getter
        @ConfigSerializable
        public static class TriStateToggleIcon {

            private int slot;

            private PermissionIcon enabled;
            private PermissionIcon unset;
            private PermissionIcon disabled;

            public TriStateToggleIcon() {
            }

            protected TriStateToggleIcon(int slot, PermissionIcon enabled, PermissionIcon unset, PermissionIcon disabled) {
                this.slot = slot;
                this.enabled = enabled;
                this.unset = unset;
                this.disabled = disabled;
            }
        }
    }
}