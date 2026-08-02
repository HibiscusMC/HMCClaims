package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class BaseListGuiConfig extends GuiTemplate {

    @Setting("valid-slots")
    private List<RangeUtil> validSlots = List.of(
            new RangeUtil(1, 7),
            new RangeUtil(10, 16)
    );

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(31)
    );

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 27);

    @Setting("claims-icon")
    private ClaimsIcon claimsIcon = new ClaimsIcon();

    @Setting("sub-claims-icon")
    private SubClaimsIcon subClaimsIcon = new SubClaimsIcon();

    @Setting("filter-icon")
    private FilterIcon filterIcon = new FilterIcon(Map.of(
            "ALL", "All",
            "MAIN", "Main Claims",
            "SUB_CLAIMS", "Sub Claims"
    ), 19);

    @Setting("search-icon")
    private SearchIcon searchIcon = new SearchIcon(25);

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 21),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 23)
    );

    @Getter
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

        private String owner = "<b><name></b> <sprite:\"minecraft:items\":item/nether_star>";

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
                    "<b><name></b> <sprite:\"minecraft:items\":item/nether_star>",
                    "<name>");
        }
    }
}