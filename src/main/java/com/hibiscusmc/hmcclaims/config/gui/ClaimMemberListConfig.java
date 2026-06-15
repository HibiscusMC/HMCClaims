package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ToString
@ConfigSerializable
public class ClaimMemberListConfig extends GuiTemplate {

    private String title = "<claim_name>";

    private int rows = 6;

    @Setting("valid-slots")
    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(10, 16),
            new RangeUtil(19, 25),
            new RangeUtil(28, 34)
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
            "example-icon", new Icon()
    );

    @Setting("filter-icon")
    private FilterIcon filterIcon = new FilterIcon();

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    @Setting("search-icon")
    private SimpleIcon searchIcon = new SimpleIcon(
            ItemUtil.build(Material.SPYGLASS, "Search", List.of("", "<white>Left-Click <gray>to search members")), 35
    );

    @Setting("add-member")
    private SimpleIcon addMemberIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Add Member", List.of("", "<white>Left-Click <gray>to add member")
    ), 40);

    private Map<String, SimpleIcon> pages = Map.of(
            "back", new SimpleIcon(ItemUtil.build(
                    Material.BARRIER, "Back", List.of("", "<white>Left-Click <gray>to go back")
            ), 45),
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 18),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 26)
    );

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Members", List.of("", "<red>You're here!")
            ), 0),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 2),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3)
    );

    @Getter
    @ToString
    @ConfigSerializable
    public static class FilterIcon {

        private ItemStack item = ItemStack.of(Material.HOPPER);

        private int slot = 27;

        private String name = "Filter Members";

        private List<String> lore = List.of(
                "",
                "<gray>- <filter_list>",
                "",
                "<white>Left-Click <gray>to select the next filter",
                "<white>Right-Click <gray>to select the previous filter"
        );

        @Setting("filter-names")
        private Map<String, String> filterNames = Map.of(
                "ALL", "All",
                "ROLE", "<role> Role"
        );

        private String selected = "<white><u><name></u> <green><b>←</b></green>";

        private String unselected = "<#c2c2c2><name>";
    }
}