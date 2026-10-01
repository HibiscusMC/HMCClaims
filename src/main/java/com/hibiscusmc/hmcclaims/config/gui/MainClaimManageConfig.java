package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Optional;

import java.util.List;
import java.util.Map;

@Config
@Getter(onMethod_ = {@Override})
@SuppressWarnings({"FieldMayBeFinal"})
public class MainClaimManageConfig extends ClaimManageConfig {

    private GuiTitle title = new GuiTitle("Manage | <claim_name>", 19);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(40)
    );

    private SimpleIcon deleteIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    private SimpleIcon renameIcon = new SimpleIcon(ConfigItem.of(
            Material.FEATHER, "Rename claim", List.of("", "<white>Left-Click <gray>to rename claim")
    ), 18);

    private SimpleIcon lockIcon = new SimpleIcon(ConfigItem.of(
            Material.CHEST, "Lock claim", List.of("", "<white>Left-Click <gray>to make your claim private")
    ), 20);

    private ConfigItem unlockIcon = ConfigItem.of(
            Material.ENDER_CHEST, "Unlock claim", List.of("", "<white>Left-Click <gray>to make your claim public")
    );

    private SimpleIcon bannedIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Banned members", List.of("", "<white>Left-Click <gray>to show the list of banned members")
    ), 22);

    @Getter
    private SimpleIcon transferIcon = new SimpleIcon(ConfigItem.of(
            Material.LEVER, "Transfer ownership", List.of("", "<white>Left-Click <gray>to transfer the claim ownership")
    ), 24);

    private SimpleIcon resizeIcon = new SimpleIcon(ConfigItem.of(
            Material.GOLDEN_HOE, "Resize claim", List.of("", "<white>Left-Click <gray>to resize claim borders")
    ), 26);

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private Map<String, SimpleIcon> tabs = MapUtil.ordered(
            "members-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 5),
            "manage-tab", new SimpleIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "Manage", List.of("", "<red>You're here!")
            ), 7)
    );

    @Optional
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();
}