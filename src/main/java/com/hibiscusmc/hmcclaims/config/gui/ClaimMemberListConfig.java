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
public class ClaimMemberListConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("<claim_name>");

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

    @Setting("not-manageable-member-icon")
    private DynamicIcon unmanageableMember = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Role: <white><role>",
            "",
            "<gray>Joined date: <white><joined_date>",
            "",
            "<red>You can't manage this member!"
    ));

    @Setting("member-icon")
    private DynamicIcon memberIcon = new DynamicIcon("<name>", List.of(
            "",
            "<gray>Role: <white><role>",
            "",
            "<gray>Joined date: <white><joined_date>",
            "",
            "<white>Left-Click <gray>to manage member"
    ));

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(53)
    );

    @Setting("filter-icon")
    private FilterIcon filterIcon = new FilterIcon(Map.of(
            "ALL", "All",
            "ROLE", "<role> Role"
    ), 46);

    @Setting("search-icon")
    private SearchIcon searchIcon = new SearchIcon(52);

    @Setting("add-member")
    private SimpleIcon addMemberIcon = new SimpleIcon(ItemUtil.build(
            Material.WRITABLE_BOOK, "Add Member", List.of("", "<white>Left-Click <gray>to add member")
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
                    Material.LIME_STAINED_GLASS_PANE, "Members", List.of("", "<red>You're here!")
            ), 1),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 7)
    );

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();
}