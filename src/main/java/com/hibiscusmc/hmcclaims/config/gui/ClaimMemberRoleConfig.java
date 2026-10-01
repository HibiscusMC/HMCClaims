package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimMemberRoleConfig extends ClaimMemberManageConfig {

    private GuiTitle title = new GuiTitle("Manage | <member_name>", 20);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(19, 25),
            new RangeUtil(28, 34),
            new RangeUtil(37, 43)
    );

    private DynamicIconWithStack roleIcon = new DynamicIconWithStack(ConfigItem.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<white>Left-Click <gray>to set this role to <white><player_name>"
    ));

    private DynamicIconWithStack roleIconSelected = new DynamicIconWithStack(ConfigItem.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<white><player_name> <red>already has this role"
    ));

    private DynamicIconWithStack roleIconUnable = new DynamicIconWithStack(ConfigItem.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<red>You can't set this role to <white><player_name>"
    ));

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
            "example-icon", new Icon(53)
    );

    private Map<String, SimpleMultiIcon> tabs = MapUtil.ordered(
            "roles-tab", new SimpleMultiIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "Roles", List.of("", "<red>You're here!")
            ), List.of(1, 2, 3)),
            "permissions-tab", new SimpleMultiIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Permissions", List.of("", "<white>Left-Click <gray>to go to this tab")
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
}