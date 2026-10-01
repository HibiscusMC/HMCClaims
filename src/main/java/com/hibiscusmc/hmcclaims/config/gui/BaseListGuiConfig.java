package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Section
@SuppressWarnings({"FieldMayBeFinal"})
public class BaseListGuiConfig extends GuiTemplate {

    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(1, 7),
            new RangeUtil(10, 16)
    );

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(31)
    );

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 27);

    private ClaimsIcon claimsIcon = new ClaimsIcon();

    private SubClaimsIcon subClaimsIcon = new SubClaimsIcon();

    private FilterIcon filterIcon = new FilterIcon(MapUtil.ordered(
            "ALL", "All",
            "MAIN", "Main Claims",
            "SUB_CLAIMS", "Sub Claims"
    ), 19);

    private SearchIcon searchIcon = new SearchIcon(25);

    private Map<String, SimpleIcon> pages = MapUtil.ordered(
            "previous-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 21),
            "next-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 23)
    );

    @Getter
    @Section
    public static class ClaimsIcon {

        private ConfigItem item = ConfigItem.of(Material.GRASS_BLOCK);

        private String name = "<gray>Name: <white><name>";

        private List<String> lore = List.of(
                "<gray>UID: <white><short_id>",
                "",
                "<gray>Private: <white><locked>",
                "<gray>Sub Claims: <white><total_sub_claims>",
                "",
                "<gray>Location: <white><world>, X: <x>, Z: <z>",
                "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
                "",
                "<gray>Members <dark_gray>(<member_count>)</dark_gray>:",
                "<gray>- <white><member_list>",
                "",
                "<gray>Creation date: <white><creation_date>",
                "",
                "<white>Left-Click <gray>to modify claim info",
                "<white>Right-Click <gray>to quick-rename your claim"
        );

        private String owner = "<b><name></b> <sprite:\"minecraft:items\":item/nether_star>";

        private String member = "<name>";

        private ClaimsIcon() {
        }

        private ClaimsIcon(ConfigItem item, String name, List<String> lore, String owner, String member) {
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.owner = owner;
            this.member = member;
        }
    }

    @Getter
    @Section
    public static class SubClaimsIcon extends ClaimsIcon {

        public SubClaimsIcon() {
            super(ConfigItem.of(Material.DIRT),
                    "<gray>Name: <white><name>",
                    List.of(
                            "<gray>UID: <white><short_id>",
                            "",
                            "<gray>Private: <white><locked>",
                            "<gray>Main Claim: <white><main_claim>",
                            "",
                            "<gray>Location: <white><world>, X: <x>, Z: <z>",
                            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
                            "",
                            "<gray>Members <dark_gray>(<member_count>)</dark_gray>:",
                            "<gray>- <white><member_list>",
                            "",
                            "<gray>Creation date: <white><creation_date>",
                            "",
                            "<white>Left-Click <gray>to modify claim info",
                            "<white>Right-Click <gray>to quick-rename your claim"
                    ),
                    "<b><name></b> <sprite:\"minecraft:items\":item/nether_star>",
                    "<name>");
        }
    }
}