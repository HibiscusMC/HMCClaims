package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimDeleteConfig extends GuiTemplate {

    private String title = "Confirm Claim Deletion";

    private int rows = 1;

    @Setting("claim-icon")
    private ClaimIcon claimIcon = new ClaimIcon();

    @Setting("subclaim-icon")
    private SubClaimIcon subClaimIcon = new SubClaimIcon();

    private SimpleIcon confirm = new SimpleIcon(
            ItemUtil.build(Material.LIME_STAINED_GLASS_PANE, "<green>Confirm"), 2
    );

    private SimpleIcon cancel = new SimpleIcon(
            ItemUtil.build(Material.RED_STAINED_GLASS_PANE, "<red>Cancel"), 6
    );

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(8)
    );

    @Setting("screen-type")
    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @Getter
    @ConfigSerializable
    public static class ClaimIcon {

        private ItemStack item = ItemStack.of(Material.GRASS_BLOCK);

        private int slot = 4;

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
                "<gray>Creation date: <white><creation_date>"
        );

        private String owner = "<b><name></b> <sprite:\"minecraft:items\":item/nether_star>";

        private String member = "<name>";

        public ClaimIcon() {
        }

        private ClaimIcon(ItemStack item, String name, List<String> lore, String owner, String member) {
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.owner = owner;
            this.member = member;
        }
    }

    @Getter
    @ConfigSerializable
    public static class SubClaimIcon extends ClaimIcon {

        public SubClaimIcon() {
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