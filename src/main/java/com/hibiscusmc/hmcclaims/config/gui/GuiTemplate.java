package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class GuiTemplate {

    @Getter
    @ConfigSerializable
    public static class GuiTitle {

        @Comment("The title of the GUI")
        private String text;

        @Setting("max-length")
        @Comment("""
                This defines the max length of the Claim name, not the title itself!
                Setting this to -1 will disable the feature.
                Claim names that exceed the length will be sliced and suffixed with "...\"""")
        private int maxLength;

        protected GuiTitle(String text) {
            this.text = text;
            this.maxLength = 28;
        }

        protected GuiTitle(String text, int maxLength) {
            this.text = text;
            this.maxLength = maxLength;
        }

        public GuiTitle() {
        }
    }

    @Getter
    @ConfigSerializable
    public static class DynamicIcon {

        private String name;

        private List<String> lore;

        public DynamicIcon() {
        }

        protected DynamicIcon(String name, List<String> lore) {
            this.name = name;
            this.lore = lore;
        }
    }

    @Getter
    @ConfigSerializable
    public static class DynamicIconWithStack {

        private ItemStack item;

        private String name;

        private List<String> lore;

        public DynamicIconWithStack() {
        }

        protected DynamicIconWithStack(ItemStack item, String name, List<String> lore) {
            this.item = item;
            this.name = name;
            this.lore = lore;
        }
    }

    @Getter
    @ConfigSerializable
    public static class SimpleIcon {

        private ItemStack item = ItemStack.of(Material.AIR);

        private int slot = -1;

        public SimpleIcon() {
        }

        protected SimpleIcon(ItemStack item, int slot) {
            this.item = item;
            this.slot = slot;
        }
    }

    @Getter
    @ConfigSerializable
    public static class SimpleMultiIcon {

        private ItemStack item = ItemStack.of(Material.AIR);

        private List<Integer> slots = List.of();

        public SimpleMultiIcon() {
        }

        protected SimpleMultiIcon(ItemStack item, List<Integer> slots) {
            this.item = item;
            this.slots = slots;
        }
    }

    @Getter
    @ConfigSerializable
    public static class Icon {

        private ItemStack item = ItemUtil.build(Material.OAK_SIGN, "<aqua>Example Icon", List.of(
                "",
                "<gray>This is an example icon!"
        ));

        private int slot;

        @Setting("left-click-actions")
        protected List<Action> leftClickActions = List.of(
                Action.parse("command: say hello!")
        );

        @Setting("right-click-actions")
        protected List<Action> rightClickActions = List.of(
                Action.parse("console: say %player_name% says hello!"),
                Action.parse("message: <green>saying hello on your behalf, <white>%player_name%</white>!")
        );

        protected Icon(int slot) {
            this.slot = slot;
        }

        public Icon() {
        }
    }

    @Getter
    @ConfigSerializable
    public static class FilterIcon {

        private ItemStack item = ItemStack.of(Material.HOPPER);

        private int slot;

        private String name = "Filter";

        private List<String> lore = List.of(
                "",
                "<gray>- <filter_list>",
                "",
                "<white>Left-Click <gray>to select the next filter",
                "<white>Right-Click <gray>to select the previous filter"
        );

        @Setting("filter-names")
        private Map<String, String> filterNames;

        private String selected = "<white><u><name></u> <green><b>←</b></green>";

        private String unselected = "<#c2c2c2><name>";

        protected FilterIcon(Map<String, String> filterNames, int slot) {
            this.slot = slot;

            this.filterNames = filterNames;
        }

        public FilterIcon() {
        }
    }

    @Getter
    @ConfigSerializable
    public static class SearchIcon {

        private ItemStack item = ItemStack.of(Material.SPYGLASS);

        private int slot;

        private String name = "Search";

        private List<String> lore = List.of(
                "",
                "<gray>Current query: <white><query>",
                "",
                "<white>Left-Click <gray>to search"
        );

        @Setting("no-query")
        private String noQuery = "<i>Nothing...";

        protected SearchIcon(int slot) {
            this.slot = slot;
        }

        public SearchIcon() {
        }
    }

    @Getter
    @ConfigSerializable
    public static class ToggleIcon<T> {

        private int slot;

        private T key;
        private DynamicIconWithStack icon;

        @Setting("has-modify-icon")
        private boolean hasModifyIcon = true;

        @Setting("modify-icon")
        private BiStateToggleIcon modifyIcon;

        public ToggleIcon() {
        }

        protected ToggleIcon(T key, int slot, String name, List<String> lore, BiStateToggleIcon modifyIcon) {
            this.key = key;

            this.icon = new DynamicIconWithStack(
                    ItemStack.of(Material.BOOK), name, lore
            );
            this.slot = slot;

            this.modifyIcon = modifyIcon;
        }

        protected static List<String> buildLore(List<String> baseLore, String value) {
            List<String> cloned = new ArrayList<>(baseLore);

            cloned.addAll(List.of(
                    "",
                    "<gray>Current Value: " + value,
                    "",
                    " <green><u>Click to change value"
            ));

            return cloned;
        }

        @Getter
        @ConfigSerializable
        public static class BiStateToggleIcon {

            private int slot;

            private DynamicIconWithStack enabled;
            private DynamicIconWithStack disabled;

            public BiStateToggleIcon() {
            }

            protected BiStateToggleIcon(int slot, DynamicIconWithStack enabled, DynamicIconWithStack disabled) {
                this.slot = slot;
                this.enabled = enabled;
                this.disabled = disabled;
            }
        }
    }

    public enum GuiScreenType {
        FULL,
        NORMAL;

        /**
         * @noinspection ProtectedMemberInFinalClass
         */
        protected final static String DESCRIPTION = """
                ┌─ Options:
                ├─ FULL
                ├   Uses both the top and the bottom inventories, using the lower part to
                │   display what is in the lower-gui config
                ├─ NORMAL
                └   Only uses the top part of the inventory""";
    }
}