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
public class ClaimBannedListConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Ban List | <claim_name>", 18);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(19, 25),
            new RangeUtil(28, 34),
            new RangeUtil(37, 43)
    );

    private DynamicIcon memberIcon = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Banned since: <white><banned_date>",
            "",
            "<white>Left-Click <gray>to unban member"
    ));

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(53)
    );

    @Key("ban-member")
    private SimpleIcon banMemberIcon = new SimpleIcon(ConfigItem.of(
            Material.WRITABLE_BOOK, "Ban Member", List.of("", "<white>Left-Click <gray>to ban member")
    ), 49);

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

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
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "Manage", List.of("", "<red>You're here!")
            ), 7)
    );

    @Optional
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();
}