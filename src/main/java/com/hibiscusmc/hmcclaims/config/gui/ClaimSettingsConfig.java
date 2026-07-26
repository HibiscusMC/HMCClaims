package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettingsConfig extends GuiTemplate {

    private GuiTitle title = new GuiTitle("Settings | <claim_name>", 17);

    private int rows = 6;

    @Setting("screen-type")
    @Comment(GuiScreenType.DESCRIPTION)
    private GuiScreenType screenType = GuiScreenType.FULL;

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.BOOK, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon(40)
    );

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<red>You're here!")
            ), 5),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 7)
    );

    @Setting("setting-pages")
    private Map<Integer, List<ToggleSettingIcon<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>>>> settingPages = buildSettingPages();

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 39),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 41)
    );

    private Map<Boolean, String> states = Map.of(
            true, "<green>Enabled",
            false, "<red>Disabled"
    );

    @Setting("lower-gui")
    private BaseListGuiConfig lowerGui = new BaseListGuiConfig();

    @NotNull
    private Map<Integer, List<ToggleSettingIcon<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>>>> buildSettingPages() {
        int startSlot = 19;
        int currentPage = 1;

        List<ToggleSettingIcon<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>>> settings = new ArrayList<>();
        Map<Integer, List<ToggleSettingIcon<com.hibiscusmc.hmcclaims.claim.setting.Setting<?>>>> pages = new HashMap<>();

        for (com.hibiscusmc.hmcclaims.claim.setting.Setting<?> setting : SettingRegistry.getAllSettings()) {
            int slot = startSlot++;
            String name = setting.displayName();
            String description = setting.description();

            List<String> lore = Arrays.stream(("<gray>" + description).split("\n")).toList();

            settings.add(new ToggleSettingIcon<>(
                    setting, slot, name, lore,
                    new ToggleIcon.BiStateToggleIcon(
                            slot + 9,
                            new DynamicIconWithStack(ItemStack.of(Material.LIME_DYE), name, ToggleIcon.buildLore(lore, "<setting_value>")),
                            new DynamicIconWithStack(ItemStack.of(Material.GRAY_DYE), name, ToggleIcon.buildLore(lore, "<setting_value>"))
                    )
            ));

            if (startSlot > 25) {
                pages.put(currentPage, settings);
                settings = new ArrayList<>();

                currentPage++;
                startSlot = 19;
            }
        }

        if (!settings.isEmpty()) {
            pages.put(currentPage, settings);
        }

        return pages;
    }

    @Getter
    @ConfigSerializable
    public static class ToggleSettingIcon<T> extends ToggleIcon<T> {

        @Setting("value-not-set")
        private String notSet = "Not Set";

        public ToggleSettingIcon() {
        }

        protected ToggleSettingIcon(T key, int slot, String name, List<String> lore, BiStateToggleIcon modifyIcon) {
            super(key, slot, name, lore, modifyIcon);
        }
    }
}