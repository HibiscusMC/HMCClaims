package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimBannedListConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Ban List | <claim_name>", 18);

    private int rows = 6;

    @Setting("screen-type")
    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    @Setting("valid-slots")
    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(19, 25),
            new RangeUtil(28, 34),
            new RangeUtil(37, 43)
    );

    @Setting("member-icon")
    private DynamicIcon memberIcon = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Banned since: <white><banned_date>",
            "",
            "<white>Left-Click <gray>to unban member"
    ));

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(53)
    );

    @Setting("ban-member")
    private SimpleIcon banMemberIcon = new SimpleIcon(ItemUtil.build(
            Material.WRITABLE_BOOK, "Ban Member", List.of("", "<white>Left-Click <gray>to ban member")
    ), 49);

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "Manage", List.of("", "<red>You're here!")
            ), 7)
    );

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();
}