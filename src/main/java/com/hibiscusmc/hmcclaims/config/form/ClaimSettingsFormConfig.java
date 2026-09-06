package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettingsFormConfig extends FormTemplate {

    private final static int PER_CATEGORY = 7;

    private Title title = new Title("Settings | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Pick a group of settings to edit."
    );

    @Setting("category-title")
    @Comment("Title of the form holding one category's settings")
    private Title categoryTitle = new Title("<category> | <claim_name>", 17);

    @Setting("always-show-categories")
    @Comment("""
            Whether the category list is shown even when there is only one category.
            With this off, a single category opens its settings form directly.""")
    private boolean alwaysShowCategories = false;

    @Setting("show-descriptions")
    @Comment("Whether each setting's description is shown above its control")
    private boolean showDescriptions = true;

    @Setting("value-not-set")
    @Comment("Placeholder shown in a text field whose setting has no value yet")
    private String valueNotSet = "Not set";

    @Setting("setting-categories")
    private Map<Integer, SettingCategory> settingCategories = buildCategories();

    private Navigation nav = new Navigation();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "categories",
            "extra:example-button",
            "nav",
            "back"
    );

    @Getter
    @ConfigSerializable
    public static class SettingEntry {

        @Comment("The setting this row edits. Remove the row to hide the setting.")
        private com.hibiscusmc.hmcclaims.claim.setting.Setting<?> key;

        @Comment("The label shown next to the control")
        private String label = "";

        @Comment("Shown above the control when show-descriptions is enabled")
        private List<String> description = List.of();

        public SettingEntry() {
        }

        public SettingEntry(com.hibiscusmc.hmcclaims.claim.setting.Setting<?> key, String label, List<String> description) {
            this.key = key;
            this.label = label;
            this.description = description;
        }
    }

    @Getter
    @ConfigSerializable
    public static class SettingCategory {

        @Comment("The category button label")
        private String name = "Category";

        private Image image = new Image();

        @Comment("Detail text shown at the top of this category's form")
        private List<String> content = List.of();

        private Map<String, SettingEntry> settings = Map.of();

        public SettingCategory() {
        }

        public SettingCategory(String name, Map<String, SettingEntry> settings) {
            this.name = name;
            this.settings = settings;
        }
    }

    @NotNull
    private static Map<Integer, SettingCategory> buildCategories() {
        Map<Integer, SettingCategory> categories = new LinkedHashMap<>();
        Map<String, SettingEntry> current = new LinkedHashMap<>();

        int page = 1;
        for (com.hibiscusmc.hmcclaims.claim.setting.Setting<?> setting : SettingRegistry.getAllSettings()) {
            List<String> description = Arrays.stream(("<gray>" + setting.description()).split("\n"))
                    .toList();

            String key = RegistryUtil.serialize(setting.key()).replace(":", "-").toLowerCase() + "-setting";
            current.put(key, new SettingEntry(setting, "<white>" + setting.displayName(), description));

            if (current.size() >= PER_CATEGORY) {
                categories.put(page, new SettingCategory("Settings " + page, current));

                current = new LinkedHashMap<>();
                page++;
            }
        }

        if (!current.isEmpty()) {
            categories.put(page, new SettingCategory(page == 1 ? "Claim settings" : "Settings " + page, current));
        }

        return categories;
    }

    @NotNull
    public static List<SettingCategory> usable(@NotNull Map<Integer, SettingCategory> categories) {
        List<SettingCategory> usable = new ArrayList<>();

        categories.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    SettingCategory category = entry.getValue();

                    boolean hasSettings = category.settings().values().stream()
                            .anyMatch(row -> row != null && row.key() != null);

                    if (hasSettings) {
                        usable.add(category);
                    }
                });

        return usable;
    }
}