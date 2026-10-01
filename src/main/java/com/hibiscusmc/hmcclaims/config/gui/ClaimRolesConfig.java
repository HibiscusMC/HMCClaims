package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Key;
import team.hypox.config.core.annotation.Optional;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimRolesConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Roles | <claim_name>", 20);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(19, 25),
            new RangeUtil(28, 34),
            new RangeUtil(37, 43)
    );

    private RoleIcon roleIcon = new RoleIcon();

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(53)
    );

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Key("create-role")
    private SimpleIcon createRoleIcon = new SimpleIcon(ConfigItem.of(
            Material.NETHER_STAR, "Create Role", List.of("", "<white>Left-Click <gray>to create a new role")
    ), 49);

    private Map<String, SimpleIcon> pages = MapUtil.ordered(
            "previous-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
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

    @Optional
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @Getter
    @Section
    public static class RoleIcon {

        private ConfigItem icon = ConfigItem.of(Material.BOOK);

        private String name = "<name>";

        private RoleIconLore lore = new RoleIconLore();

        private boolean enablePositionSwap = true;

        @Getter
        @Section
        public static class RoleIconLore {

            private List<String> base = List.of(
                    "",
                    "<gray>Members: <white><members>",
                    "",
                    "<gray>Creation date: <white><creation_date>",
                    "",
                    "<manage>",
                    "<rename>",
                    "",
                    "<swap_previous>",
                    "<swap_next>"
            );

            private String manage = "<white>Left-Click <gray>to manage role";
            private String cantManage = "<red>You can't manage this role";

            private String rename = "<white>Right-Click <gray>to rename this role";
            private String cantRename = "<red>You can't rename this role";

            private String swapPrevious = "<white>Shift + Left-Click <gray>to swap positions with the previous role";
            private String cantSwapPrevious = "<red>You can't swap positions with the previous role";

            private String swapNext = "<white>Shift + Right-Click <gray>to swap positions with the next role";
            private String cantSwapNext = "<red>You can't swap positions with the next role";
        }
    }
}