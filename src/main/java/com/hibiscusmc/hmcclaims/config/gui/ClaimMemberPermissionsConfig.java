package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimMemberPermissionsConfig extends ClaimMemberManageConfig {

    private GuiTitle title = new GuiTitle("Manage | <member_name>", 20);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private Map<String, String> states = MapUtil.ordered(
            "enabled", "<green>Enabled",
            "unset", "<gray>Unset",
            "disabled", "<red>Disabled"
    );

    private MemberPermissionList permissionList = new MemberPermissionList();

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private SimpleIcon kickIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Kick Member", List.of("", "<white>Left-Click <gray>to kick this member")
    ), 47);

    private ConfigItem cantKickIcon = ConfigItem.of(
            Material.BARRIER, "Kick Member", List.of("", "<red>You can't kick this member")
    );

    private SimpleIcon banIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Ban Member", List.of("", "<white>Left-Click <gray>to ban this member")
    ), 51);

    private ConfigItem cantBanIcon = ConfigItem.of(
            Material.BARRIER, "Ban Member", List.of("", "<red>You can't ban this member")
    );

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleMultiIcon> tabs = MapUtil.ordered(
            "roles-tab", new SimpleMultiIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), List.of(1, 2, 3)),
            "permissions-tab", new SimpleMultiIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "Permissions", List.of("", "<red>You're here!")
            ), List.of(5, 6, 7))
    );

    private Map<String, SimpleIcon> pages = MapUtil.ordered(
            "previous-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );

    @Getter
    @Section
    public static class MemberPermissionList extends PermissionToggleList {

        private MemberToggles toggle = new MemberToggles();
    }

    @Getter
    @Section
    public static class MemberToggles {

        private PermissionToggleIcon enabled = permissionToggle(Material.LIME_DYE);

        private PermissionToggleIcon unset = permissionToggle(Material.GRAY_DYE);

        private PermissionToggleIcon disabled = permissionToggle(Material.RED_DYE);

        private static PermissionToggleIcon permissionToggle(Material material) {
            return new PermissionToggleIcon(
                    material, "<permission_name>",
                    ToggleList.lore("permission", "permission_value", " <green><u>Click to change value"),
                    ToggleList.lore("permission", "permission_value", "<red>You don't have permissions to change this")
            );
        }
    }
}
