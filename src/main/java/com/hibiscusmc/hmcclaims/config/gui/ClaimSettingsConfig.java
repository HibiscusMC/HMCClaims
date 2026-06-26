package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettingsConfig extends GuiTemplate {

    private String title = "Claim Setting";

    private int rows = 5;

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon()
    );

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 0),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Settings", List.of("", "<red>You're here!")
            ), 2),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Manage", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 3)
    );

    private List<SettingIcon<?>> settings = List.of(
            new SettingIcon<>(com.hibiscusmc.hmcclaims.claim.setting.Setting.MOB_EXPLOSIONS, 19),
            new SettingIcon<>(com.hibiscusmc.hmcclaims.claim.setting.Setting.BLOCK_EXPLOSIONS, 21),
            new SettingIcon<>(com.hibiscusmc.hmcclaims.claim.setting.Setting.JOIN_MESSAGE, 23)
    );

    @Getter
    @ConfigSerializable
    public static class SettingIcon<T> {

        private com.hibiscusmc.hmcclaims.claim.setting.Setting<T> setting;
        private SimpleIcon icon;

        @Setting("has-modify-icon")
        private boolean hasModifyIcon = true;

        @Setting("modify-icon")
        private BaseSettingIcon modifyIcon;

        public SettingIcon(com.hibiscusmc.hmcclaims.claim.setting.Setting<T> setting, int slot) {
            this.setting = setting;

            List<String> lore = Arrays.stream(setting.description().split("\n")).toList();

            this.icon = new SimpleIcon(
                    ItemUtil.build(Material.BOOK, setting.displayName(), lore),
                    slot
            );

            int nextSlot = slot + 9;

            if (setting.defaultValue() instanceof Boolean) {
                modifyIcon = new BooleanSettingIcon(
                        new SimpleIcon(ItemUtil.build(Material.GREEN_DYE, setting.displayName(), buildLore(lore, "Enabled")), nextSlot),
                        new SimpleIcon(ItemUtil.build(Material.GRAY_DYE, setting.displayName(), buildLore(lore, "Disabled")), nextSlot)
                );
            } else {
                modifyIcon = new InputSettingIcon(
                        new SimpleIcon(ItemUtil.build(Material.PAPER, "Input", buildLore(lore, setting.defaultValue().toString())), nextSlot));
            }
        }

        private List<String> buildLore(List<String> baseLore, String defaultValue) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "Current Status: " + defaultValue,
                    "",
                    "Click to change value"
            ));

            return cloned;
        }

        @ConfigSerializable
        public abstract static class BaseSettingIcon {
        }

        @Getter
        @ConfigSerializable
        public static class BooleanSettingIcon extends BaseSettingIcon {

            private SimpleIcon enabled;
            private SimpleIcon disabled;

            public BooleanSettingIcon(SimpleIcon enabled, SimpleIcon disabled) {
                this.enabled = enabled;
                this.disabled = disabled;
            }
        }

        @Getter
        @ConfigSerializable
        public static class InputSettingIcon extends BaseSettingIcon {

            private SimpleIcon input;

            public InputSettingIcon(SimpleIcon input) {
                this.input = input;
            }
        }
    }
}