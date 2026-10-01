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

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimMemberListConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Members | <claim_name>", 18);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(19, 25),
            new RangeUtil(28, 34),
            new RangeUtil(37, 43)
    );

    @Key("not-manageable-member-icon")
    private DynamicIcon unmanageableMember = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Role: <white><role>",
            "",
            "<gray>Joined date: <white><joined_date>",
            "",
            "<red>You can't manage this member!"
    ));

    private DynamicIcon memberIcon = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Role: <white><role>",
            "",
            "<gray>Joined date: <white><joined_date>",
            "",
            "<white>Left-Click <gray>to manage member"
    ));

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(53)
    );

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private FilterIcon filterIcon = new FilterIcon(MapUtil.ordered(
            "ALL", "All",
            "ROLE", "<role> Role"
    ), 46);

    private SearchIcon searchIcon = new SearchIcon(52);

    @Key("add-member")
    private SimpleIcon addMemberIcon = new SimpleIcon(ConfigItem.of(
            Material.WRITABLE_BOOK, "Add Member", List.of("", "<white>Left-Click <gray>to add member")
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
                    Material.LIME_STAINED_GLASS_PANE, "Members", List.of("", "<red>You're here!")
            ), 1),
            "roles-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
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
}