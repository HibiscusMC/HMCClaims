package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
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
public class Guis {

    @Getter
    @ToString
    @ConfigSerializable
    public static class SimpleIcon {

        private ItemStack item = ItemStack.of(Material.STONE);

        private int slot = 0;

        public SimpleIcon() {
        }

        private SimpleIcon(ItemStack item, int slot) {
            this.item = item;
            this.slot = slot;
        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class Icon {

        private ItemStack item = ItemUtil.build(Material.PAPER, "<aqua>Example Icon", List.of(
                "",
                "<gray>This is an example icon!"
        ));

        private int slot = 40;

        @Setting("left-click-actions")
        private List<Action> leftClickActions = List.of(
                Action.parse("command: say hello!")
        );

        @Setting("right-click-actions")
        private List<Action> rightClickActions = List.of(
                Action.parse("console: say %player_name% says hello!"),
                Action.parse("message: <green>saying hello on your behalf, <white>%player_name%</white>!")
        );
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class ClaimList extends Guis {

        private String title = "Your claims";

        @Setting("claims-icon")
        private ClaimsIcon claimsIcon = new ClaimsIcon();

        @Setting("sub-claims-icon")
        private SubClaimsIcon subClaimsIcon = new SubClaimsIcon();

        @Setting("filter-icon")
        private FilterIcon filterIcon = new FilterIcon();

        @Setting("search-icon")
        private SearchIcon searchIcon = new SearchIcon();

        @Setting("extra-icons")
        private Map<String, Icon> extraIcons = Map.of(
                "example-icon", new Icon()
        );

        private Map<String, SimpleIcon> pages = Map.of(
                "previous-page", new SimpleIcon(ItemUtil.build(
                        Material.STONE_BUTTON, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous")
                ), 39),
                "next-page", new SimpleIcon(ItemUtil.build(
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
                                "<gray>Inherits Permissions: <white><inherits_permissions>",
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
        public static class SearchIcon {

            private ItemStack item = ItemStack.of(Material.SPYGLASS);

            private int slot = 44;

            private String name = "Search...";

            private List<String> lore = List.of(
                    "",
                    "<white>Left-Click <gray>to search claims"
            );
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

    @Getter
    @ToString
    @ConfigSerializable
    public static class ClaimInfo extends Guis {

        private String title = "<claim_name> Info";
    }
}