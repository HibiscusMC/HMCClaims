package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Optional;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimRoleManageConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Permissions | <role_name> Role", 20);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private Map<String, SimpleIcon> pages = MapUtil.ordered(
            "previous-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleIcon> tabs = MapUtil.ordered(
            "members-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "Roles", List.of("", "<red>You're here!")
            ), 3),
            "settings-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 7)
    );

    private SimpleIcon deleteIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Delete role", List.of("", "<white>Left-Click <gray>to delete role")
    ), 53);

    private ConfigItem cantDeleteIcon = ConfigItem.of(
            Material.BARRIER, "Delete role", List.of("", "<red>You can't delete this role")
    );

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private Map<String, String> states = MapUtil.ordered(
            "enabled", "<green>Enabled",
            "disabled", "<red>Disabled"
    );

    private RolePermissionList permissionList = new RolePermissionList();

    @Optional
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @Getter
    @Section
    public static class RolePermissionList extends PermissionToggleList {

        private RoleToggles toggle = new RoleToggles();
    }

    @Getter
    @Section
    public static class RoleToggles {

        private PermissionToggleIcon enabled = permissionToggle(Material.LIME_DYE);

        private PermissionToggleIcon disabled = permissionToggle(Material.GRAY_DYE);

        private static PermissionToggleIcon permissionToggle(Material material) {
            return new PermissionToggleIcon(
                    material, "<permission_name>",
                    ToggleList.lore("permission", "permission_value", " <green><u>Click to change value"),
                    ToggleList.lore("permission", "permission_value", "<red>You don't have permissions to change this")
            );
        }
    }
}