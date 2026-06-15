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
public class ClaimListConfig extends GuiTemplate {

    private String title = "Your claims";

    private int rows = 5;

    @Setting("valid-slots")
    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(0, 0),
            new RangeUtil(9, rows * 9)
    );

    @Setting("claims-icon")
    private ClaimsIcon claimsIcon = new ClaimsIcon();

    @Setting("sub-claims-icon")
    private SubClaimsIcon subClaimsIcon = new SubClaimsIcon();

    @Setting("filter-icon")
    private FilterIcon filterIcon = new FilterIcon();

    @Setting("search-icon")
    private SimpleIcon searchIcon = new SimpleIcon(
            ItemUtil.build(Material.SPYGLASS, "Search", List.of("", "<white>Left-Click <gray>to search claims")), 44
    );

    @Setting("extra-icons")
    private Map<String, GuiTemplate.Icon> extraIcons = Map.of(
            "example-icon", new GuiTemplate.Icon()
    );

    private Map<String, GuiTemplate.SimpleIcon> pages = Map.of(
            "previous-page", new GuiTemplate.SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 39),
            "next-page", new GuiTemplate.SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 41)
    );

    @Getter
    @ToString
    @ConfigSerializable
    public static class ClaimsIcon {

        private ItemStack item = ItemStack.of(Material.GRASS_BLOCK);

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

        private String owner = "<b><name></b> <sprite:blocks:item/nether_star>";

        private String member = "<name>";

        private ClaimsIcon() {
        }

        private ClaimsIcon(ItemStack item, String name, List<String> lore, String owner, String member) {
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.owner = owner;
            this.member = member;
        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class SubClaimsIcon extends ClaimsIcon {

        public SubClaimsIcon() {
            super(ItemStack.of(Material.DIRT),
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
                    "<b><name></b> <sprite:blocks:item/nether_star>",
                    "<name>");
        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class FilterIcon {

        private ItemStack item = ItemStack.of(Material.HOPPER);

        private int slot = 36;

        private String name = "Filter Claims";

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
                "MAIN", "Main Claims",
                "SUB_CLAIMS", "Sub Claims"
        );

        private String selected = "<white><u><name></u> <green><b>←</b></green>";

        private String unselected = "<#c2c2c2><name>";
    }
}