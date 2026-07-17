package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class GuiTemplate {

    @Getter
    @ConfigSerializable
    public static class GuiTitle {

        private String text;

        @Setting("max-length")
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

        private ItemStack item = ItemStack.of(Material.STONE);

        private int slot = 0;

        public SimpleIcon() {
        }

        protected SimpleIcon(ItemStack item, int slot) {
            this.item = item;
            this.slot = slot;
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

    public enum GuiScreenType {
        FULL,
        NORMAL
    }
}