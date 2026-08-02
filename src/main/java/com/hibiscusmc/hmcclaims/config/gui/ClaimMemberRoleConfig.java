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
public class ClaimMemberRoleConfig extends ClaimMemberManageConfig {

    private GuiTitle title = new GuiTitle("Manage | <member_name>", 20);

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
    private DynamicIconWithStack roleIcon = new DynamicIconWithStack(ItemStack.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<white>Left-Click <gray>to set this role to <white><player_name>"
    ));

    @Setting("role-icon-selected")
    private DynamicIconWithStack roleIconSelected = new DynamicIconWithStack(ItemStack.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<white><player_name> <red>already has this role"
    ));

    @Setting("role-icon-unable")
    private DynamicIconWithStack roleIconUnable = new DynamicIconWithStack(ItemStack.of(Material.BOOK),
            "<name>", List.of(
            "",
            "<gray>Members: <white><members>",
            "",
            "<gray>Creation date: <white><creation_date>",
            "",
            "<red>You can't set this role to <white><player_name>"
    ));

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Setting("kick-icon")
    private SimpleIcon kickIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Kick Member", List.of("", "<white>Left-Click <gray>to kick this member")
    ), 47);

    @Setting("cant-kick-icon")
    private ItemStack cantKickIcon = ItemUtil.build(
            Material.BARRIER, "Kick Member", List.of("", "<red>You can't kick this member")
    );

    @Setting("ban-icon")
    private SimpleIcon banIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Ban Member", List.of("", "<white>Left-Click <gray>to ban this member")
    ), 51);

    @Setting("cant-ban-icon")
    private ItemStack cantBanIcon = ItemUtil.build(
            Material.BARRIER, "Ban Member", List.of("", "<red>You can't ban this member")
    );

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(53)
    );

    private Map<String, SimpleMultiIcon> tabs = Map.of(
            "roles-tab", new SimpleMultiIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "Roles", List.of("", "<red>You're here!")
            ), List.of(1, 2, 3)),
            "permissions-tab", new SimpleMultiIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Permissions", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), List.of(5, 6, 7))
    );

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 48),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 50)
    );
}