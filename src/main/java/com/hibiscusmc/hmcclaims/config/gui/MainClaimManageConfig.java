package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@ConfigSerializable
@Getter(onMethod_ = {@Override})
@SuppressWarnings({"FieldMayBeFinal"})
public class MainClaimManageConfig extends ClaimManageConfig {

    private GuiTitle title = new GuiTitle("<claim_name>");

    private int rows = 6;

    @Setting("screen-type")
    private GuiScreenType screenType = GuiScreenType.FULL;

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(40)
    );

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    @Setting("rename-icon")
    private SimpleIcon renameIcon = new SimpleIcon(ItemUtil.build(
            Material.FEATHER, "Rename claim", List.of("", "<white>Left-Click <gray>to rename claim")
    ), 18);

    @Setting("lock-icon")
    private SimpleIcon lockIcon = new SimpleIcon(ItemUtil.build(
            Material.CHEST, "Lock claim", List.of("", "<white>Left-Click <gray>to make your claim private")
    ), 20);

    @Setting("unlock-icon")
    private ItemStack unlockIcon = ItemUtil.build(
            Material.ENDER_CHEST, "Unlock claim", List.of("", "<white>Left-Click <gray>to make your claim public")
    );

    @Setting("banned-icon")
    private SimpleIcon bannedIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Banned members", List.of("", "<white>Left-Click <gray>to show the list of banned members")
    ), 22);

    @Getter
    @Setting("transfer-icon")
    private SimpleIcon transferIcon = new SimpleIcon(ItemUtil.build(
            Material.LEVER, "Transfer ownership", List.of("", "<white>Left-Click <gray>to transfer the claim ownership")
    ), 24);

    @Setting("resize-icon")
    private SimpleIcon resizeIcon = new SimpleIcon(ItemUtil.build(
            Material.GOLDEN_HOE, "Resize claim", List.of("", "<white>Left-Click <gray>to resize claim borders")
    ), 26);

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.BOOK, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<red>You're here!")
            ), 7)
    );

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();
}