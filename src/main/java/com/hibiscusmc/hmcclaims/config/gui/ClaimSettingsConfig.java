package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

    private int rows = 6;

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

            if (setting.defaultValue() instanceof Boolean) {
                modifyIcon = new BooleanSettingIcon(
                        nextSlot,
                        new DynamicIconWithStack(ItemStack.of(Material.LIME_DYE), setting.displayName(), buildLore(lore, "<green>Enabled")),
                        new DynamicIconWithStack(ItemStack.of(Material.GRAY_DYE), setting.displayName(), buildLore(lore, "<red>Disabled"))
                );
            } else {
                modifyIcon = new InputSettingIcon(
                        nextSlot,
                        new DynamicIconWithStack(ItemStack.of(Material.FEATHER), "Input", buildLore(lore, "<setting_value>")));
            }
        }

        private List<String> buildLore(List<String> baseLore, String defaultValue) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "<gray>Current Status: <white>" + defaultValue,
                    "",
                    " <green><u>Click to change value"
            ));

            return cloned;
        }

        public abstract static class BaseSettingIcon {
        }

        @Getter
        @ConfigSerializable
        public static class BooleanSettingIcon extends BaseSettingIcon {

            private int slot;
            private DynamicIconWithStack enabled;
            private DynamicIconWithStack disabled;

            public BooleanSettingIcon() {
            }

            protected BooleanSettingIcon(int slot, DynamicIconWithStack enabled, DynamicIconWithStack disabled) {
                this.slot = slot;
                this.enabled = enabled;
                this.disabled = disabled;
            }
        }

        @Getter
        @ConfigSerializable
        public static class InputSettingIcon extends BaseSettingIcon {

            private int slot;
            private DynamicIconWithStack input;

            public InputSettingIcon() {
            }

            protected InputSettingIcon(int slot, DynamicIconWithStack input) {
                this.slot = slot;
                this.input = input;
            }
        }
    }
}