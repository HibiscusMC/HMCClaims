package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Key;
import team.hypox.config.core.annotation.Optional;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettingsConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Settings | <claim_name>", 17);

    private int rows = 6;

    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    private SimpleIcon deleteIcon = new SimpleIcon(ConfigItem.of(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
            Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private Map<String, Icon> extraIcons = MapUtil.ordered(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleIcon> tabs = MapUtil.ordered(
            "members-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ConfigItem.of(
                    Material.LIME_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<red>You're here!")
            ), 5),
            "manage-tab", new SimpleIcon(ConfigItem.of(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 7)
    );

    private SettingList settingList = new SettingList();

    private Map<String, SimpleIcon> pages = MapUtil.ordered(
            "previous-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 39),
            "next-page", new SimpleIcon(ConfigItem.of(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 41)
    );

    private Map<Boolean, String> states = MapUtil.ordered(
            true, "<green>Enabled",
            false, "<red>Disabled"
    );

    @Optional
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @Getter
    @Section
    public static class SettingList extends ToggleList {

        private SettingToggles toggle = new SettingToggles();

        @Key("value-not-set")
        private String notSet = "Not Set";

        public SettingList() {
            super("setting");
        }
    }

    @Getter
    @Section
    public static class SettingToggles {

        private DynamicIconWithStack enabled = settingToggle(Material.LIME_DYE);

        private DynamicIconWithStack disabled = settingToggle(Material.GRAY_DYE);

        private static DynamicIconWithStack settingToggle(Material material) {
            return new DynamicIconWithStack(
                    ConfigItem.of(material), "<setting_name>",
                    ToggleList.lore("setting", "setting_value", " <green><u>Click to change value")
            );
        }
    }
}