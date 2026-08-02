package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
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
public class ClaimRolesConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Roles | <claim_name>", 20);

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

    @Setting("role-icon")
    private RoleIcon roleIcon = new RoleIcon();

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(53)
    );

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Setting("create-role")
    private SimpleIcon createRoleIcon = new SimpleIcon(ItemUtil.build(
            Material.NETHER_STAR, "Create Role", List.of("", "<white>Left-Click <gray>to create a new role")
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
                    Material.LIME_STAINED_GLASS_PANE, "Roles", List.of("", "<red>You're here!")
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

    @Getter
    @ConfigSerializable
    public static class RoleIcon {

        private ItemStack icon = ItemStack.of(Material.BOOK);

        private String name = "<name>";

        private RoleIconLore lore = new RoleIconLore();

        @Setting("enable-position-swap")
        private boolean enablePositionSwap = true;

        @Getter
        @ConfigSerializable
        public static class RoleIconLore {

            private List<String> base = List.of(
                    "",
                    "<gray>Members: <white><members>",
                    "",
                    "<gray>Creation date: <white><creation_date>",
                    "",
                    "<manage>",
                    "<rename>",
                    "",
                    "<swap_previous>",
                    "<swap_next>"
            );

            private String manage = "<white>Left-Click <gray>to manage role";
            @Setting("cant-manage")
            private String cantManage = "<red>You can't manage this role";

            private String rename = "<white>Right-Click <gray>to rename this role";
            @Setting("cant-rename")
            private String cantRename = "<red>You can't rename this role";

            @Setting("swap-previous")
            private String swapPrevious = "<white>Shift + Left-Click <gray>to swap positions with the previous role";
            @Setting("cant-swap-previous")
            private String cantSwapPrevious = "<red>You can't swap positions with the previous role";

            @Setting("swap-next")
            private String swapNext = "<white>Shift + Right-Click <gray>to swap positions with the next role";
            @Setting("cant-swap-next")
            private String cantSwapNext = "<red>You can't swap positions with the next role";
        }
    }
}