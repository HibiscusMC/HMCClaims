package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
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

    private GuiTitle title = new GuiTitle("<claim_name>");

    private int rows = 6;

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
            ), 0),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.LIME_STAINED_GLASS_PANE, "<gray>Settings", List.of("", "<red>You're here!")
            ), 2),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.GRAY_STAINED_GLASS_PANE, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3)
    );

    @Setting("setting-pages")
    private Map<Integer, List<SettingIcon<?>>> settingPages = buildSettingPages();

    private Map<String, SimpleIcon> pages = Map.of(
            "previous-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Previous Page", List.of("", "<white>Left-Click <gray>to go to the previous page")
            ), 39),
            "next-page", new SimpleIcon(ItemUtil.build(
                    Material.ARROW, "Next Page", List.of("", "<white>Left-Click <gray>to go to the next page")
            ), 41)
    );

    private Map<Integer, List<SettingIcon<?>>> buildSettingPages() {
        int startSlot = 19;
        int currentPage = 1;

        List<SettingIcon<?>> settings = new ArrayList<>();
        Map<Integer, List<SettingIcon<?>>> pages = new HashMap<>();

        for (com.hibiscusmc.hmcclaims.claim.setting.Setting<?> setting : SettingRegistry.getAllSettings()) {
            settings.add(new SettingIcon<>(setting, startSlot++));

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
    public static class SettingIcon<T> {

        private com.hibiscusmc.hmcclaims.claim.setting.Setting<T> setting;
        private SimpleIcon icon;

        @Setting("has-modify-icon")
        private boolean hasModifyIcon = true;

        @Setting("modify-icon")
        private BooleanSettingIcon modifyIcon;

        public SettingIcon() {
        }

        protected SettingIcon(com.hibiscusmc.hmcclaims.claim.setting.Setting<T> setting, int slot) {
            this.setting = setting;

            List<String> lore = Arrays.stream(("<gray>" + setting.description()).split("\n")).toList();

            this.icon = new SimpleIcon(
                    ItemUtil.build(Material.BOOK, setting.displayName(), lore),
                    slot
            );

            int nextSlot = slot + 9;

            modifyIcon = new BooleanSettingIcon(
                    nextSlot,
                    new DynamicIconWithStack(ItemStack.of(Material.LIME_DYE), setting.displayName(), buildLore(lore, setting.defaultValue() instanceof Boolean ? "<green>Enabled" : "<white><setting_value>")),
                    new DynamicIconWithStack(ItemStack.of(Material.GRAY_DYE), setting.displayName(), buildLore(lore, "<red>Disabled"))
            );
        }

        private List<String> buildLore(List<String> baseLore, String defaultValue) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "<gray>Current Value: " + defaultValue,
                    "",
                    " <green><u>Click to change value"
            ));

            return cloned;
        }

        @Getter
        @ConfigSerializable
        public static class BooleanSettingIcon {

            private int slot;

            private DynamicIconWithStack enabled;
            private DynamicIconWithStack disabled;

            @Setting("not-set")
            private String notSet = "Not Set";

            public BooleanSettingIcon() {
            }

            protected BooleanSettingIcon(int slot, DynamicIconWithStack enabled, DynamicIconWithStack disabled) {
                this.slot = slot;
                this.enabled = enabled;
                this.disabled = disabled;
            }
        }
    }
}