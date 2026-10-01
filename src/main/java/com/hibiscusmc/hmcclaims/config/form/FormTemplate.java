package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Section
@SuppressWarnings({"FieldMayBeFinal"})
public class FormTemplate {

    public final static String ORDER_DESCRIPTION
            = "The sections of this form, top to bottom. Remove one to hide it. extra:<key> places an extra button.";

    /**
     * How a button icon is sourced.
     * <p>
     * {@code NONE} falls back to the material of the matching inventory-GUI icon when
     * {@code config.yml -> forms.material-image-fallback} is on, {@code PATH} points at a
     * texture inside the Bedrock client, and {@code URL} at an image it downloads.
     */
    public enum ImageType {
        NONE,
        PATH,
        URL
    }

    @Getter
    @Section
    public static class Image {

        @Comment("NONE, PATH (a texture inside the Bedrock client) or URL")
        private ImageType type = ImageType.NONE;

        private String data = "";

        public Image() {
        }

        public Image(ImageType type, String data) {
            this.type = type;
            this.data = data;
        }

        public static Image path(String path) {
            return new Image(ImageType.PATH, path);
        }
    }

    @Getter
    @Section
    public static class Button {

        @Comment("The button label")
        private String text = "Button";

        private Image image = new Image();

        public Button() {
        }

        public Button(String text) {
            this.text = text;
        }

        public Button(String text, Image image) {
            this.text = text;
            this.image = image;
        }
    }

    @Getter
    @Section
    public static class ActionButton extends Button {

        private List<Action> actions = List.of(
                Action.parse("message: <green>Hello, <white>%player_name%</white>!")
        );

        public ActionButton() {
        }

        public ActionButton(String text) {
            super(text);
        }
    }

    @Getter
    @Section
    public static class Entry {

        @Comment("The row label, one entry per rendered line. Placeholders are supported.")
        private List<String> text = List.of("<name>");

        private Image image = new Image();

        public Entry() {
        }

        public Entry(List<String> text, Image image) {
            this.text = text;
            this.image = image;
        }
    }

    @Getter
    @Section
    public static class Title {

        @Comment("The title shown at the top of the form")
        private String text = "";

        @Comment("Max length of the claim name shown in the title, not of the title itself. -1 disables it.")
        private int maxLength = 28;

        public Title() {
        }

        public Title(String text) {
            this.text = text;
        }

        public Title(String text, int maxLength) {
            this.text = text;
            this.maxLength = maxLength;
        }
    }

    @Getter
    @Section
    public static class SubForm {

        private Title title = new Title("<name>");

        @Comment("The detail text shown above the buttons")
        private List<String> content = List.of();

        @Comment("The available actions. Remove one to hide it.")
        private Map<String, Button> buttons = Map.of();

        public SubForm() {
        }

        public SubForm(Title title, List<String> content, Map<String, Button> buttons) {
            this.title = title;
            this.content = content;
            this.buttons = buttons;
        }
    }

    @Getter
    @Section
    public static class Input {

        private String label = "Value";

        @Comment("The greyed-out hint shown while the field is empty")
        private String placeholder = "";

        @Comment("Currently only used to trim overly long submissions. -1 disables it.")
        private int maxLength = 100;

        public Input() {
        }

        public Input(String label, String placeholder) {
            this.label = label;
            this.placeholder = placeholder;
        }

        public Input(String label, String placeholder, int maxLength) {
            this.label = label;
            this.placeholder = placeholder;
            this.maxLength = maxLength;
        }
    }

    @Getter
    @Section
    public static class Dropdown {

        private String label = "Option";

        @Comment("Maps the internal id to the label shown to the player")
        private Map<String, String> options = Map.of();

        public Dropdown() {
        }

        public Dropdown(String label, Map<String, String> options) {
            this.label = label;
            this.options = options;
        }
    }

    @Getter
    @Section
    public static class Navigation {

        @Comment("Remove an entry to hide that destination")
        private Map<String, Button> buttons = MapUtil.ordered(
                "members", new Button("Members", Image.path("textures/ui/FriendsIcon")),
                "roles", new Button("Roles", Image.path("textures/ui/permissions_op_crown")),
                "settings", new Button("Settings", Image.path("textures/ui/settings_glyph_color_2x")),
                "manage", new Button("Manage", Image.path("textures/ui/hammer_l"))
        );

        @Comment("Label of the navigation dropdown on forms that can't hold buttons.")
        private String dropdownLabel = "<gray>Go to";

        @Comment("The dropdown option that keeps the player where they are")
        private String dropdownStay = "Stay here";
    }
}