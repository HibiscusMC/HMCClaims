package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Key;
import team.hypox.config.core.annotation.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
@Section
@SuppressWarnings({"FieldMayBeFinal"})
public class GuiTemplate {

    @Getter
    @Section
    public static class GuiTitle {

        @Comment("The title of the GUI")
        private String text;

        @Comment("Max length of the claim name shown in the title, not of the title itself. -1 disables it.")
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
    @Section
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
    @Section
    public static class DynamicIconWithStack {

        private ConfigItem item;

        private String name;

        private List<String> lore;

        public DynamicIconWithStack() {
        }

        protected DynamicIconWithStack(ConfigItem item, String name, List<String> lore) {
            this.item = item;
            this.name = name;
            this.lore = lore;
        }
    }

    @Getter
    @Section
    public static class SimpleIcon {

        private ConfigItem item = ConfigItem.of(Material.AIR);

        private int slot = -1;

        public SimpleIcon() {
        }

        protected SimpleIcon(ConfigItem item, int slot) {
            this.item = item;
            this.slot = slot;
        }
    }

    @Getter
    @Section
    public static class SimpleMultiIcon {

        private ConfigItem item = ConfigItem.of(Material.AIR);

        private List<Integer> slots = List.of();

        public SimpleMultiIcon() {
        }

        protected SimpleMultiIcon(ConfigItem item, List<Integer> slots) {
            this.item = item;
            this.slots = slots;
        }
    }

    @Getter
    @Section
    public static class Icon {

        private ConfigItem item = ConfigItem.of(Material.OAK_SIGN, "<aqua>Example Icon", List.of(
                "",
                "<gray>This is an example icon!"
        ));

        private int slot;

        protected List<Action> leftClickActions = List.of(
                Action.parse("command: say hello!")
        );

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
    @Section
    public static class FilterIcon {

        private ConfigItem item = ConfigItem.of(Material.HOPPER);

        private int slot;

        private String name = "Filter";

        private List<String> lore = List.of(
                "",
                "<gray>- <filter_list>",
                "",
                "<white>Left-Click <gray>to select the next filter",
                "<white>Right-Click <gray>to select the previous filter"
        );

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
    @Section
    public static class SearchIcon {

        private ConfigItem item = ConfigItem.of(Material.SPYGLASS);

        private int slot;

        private String name = "Search";

        private List<String> lore = List.of(
                "",
                "<gray>Current query: <white><query>",
                "",
                "<white>Left-Click <gray>to search"
        );

        private String noQuery = "<i>Nothing...";

        protected SearchIcon(int slot) {
            this.slot = slot;
        }

        public SearchIcon() {
        }
    }

    @Getter
    @Section
    public static class PermissionToggleIcon extends DynamicIconWithStack {

        @Key("cant-change-lore")
        private List<String> cantChangeLore = List.of();

        public PermissionToggleIcon() {
        }

        protected PermissionToggleIcon(Material material, String name, List<String> lore, List<String> cantChangeLore) {
            super(ConfigItem.of(material), name, lore);

            this.cantChangeLore = cantChangeLore;
        }
    }

    /**
     * The slots a registry-driven list (permissions or settings) is drawn in. Every entry takes one
     * of the {@code slots} and its toggle goes {@code toggle-offset} slots after it.
     */
    @Getter
    @Section
    public static class ToggleList {

        @Comment("The slots the entries are placed in. The rest go to the next pages.")
        private List<RangeUtil> slots = List.of(new RangeUtil(19, 25));

        @Key("toggle-offset")
        @Comment("The toggle goes in the entry's slot plus this. Set to 0 to hide the toggles.")
        private int toggleOffset = 9;

        private DynamicIcon icon;

        public ToggleList() {
        }

        protected ToggleList(String type) {
            this.icon = new DynamicIcon("<" + type + "_name>", List.of("<" + type + "_description>"));
        }

        public boolean hasToggle() {
            return toggleOffset != 0;
        }

        public int[] allSlots() {
            return slots.stream()
                    .flatMapToInt(range -> IntStream.of(range.all()))
                    .toArray();
        }

        public <T> List<List<T>> pages(List<T> entries) {
            int size = Math.max(1, allSlots().length);
            List<List<T>> pages = new ArrayList<>();

            for (int i = 0; i < entries.size(); i += size) {
                pages.add(entries.subList(i, Math.min(entries.size(), i + size)));
            }

            return pages;
        }

        protected static List<String> lore(String type, String value, String footer) {
            return List.of(
                    "<" + type + "_description>",
                    "",
                    "<gray>Current Value: <" + value + ">",
                    "",
                    footer
            );
        }
    }

    /**
     * A toggle list whose entries can be read-only for the viewer, shown with the no-access icon.
     */
    @Getter
    @Section
    public static class PermissionToggleList extends ToggleList {

        @Key("no-access-icon")
        private DynamicIcon noAccessIcon = new DynamicIcon("<permission_name>", List.of("<permission_description>"));

        public PermissionToggleList() {
            super("permission");
        }
    }

    public enum GuiScreenType {
        FULL,
        NORMAL;

        /**
         * @noinspection ProtectedMemberInFinalClass
         */
        protected final static String DESCRIPTION
                = "FULL also uses the player inventory to show the lower-gui, NORMAL only the top one.";
    }
}