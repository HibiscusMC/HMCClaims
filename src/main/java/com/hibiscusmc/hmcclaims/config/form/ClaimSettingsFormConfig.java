package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimSettingsFormConfig extends FormTemplate {

    private Title title = new Title("Settings | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Pick a group of settings to edit."
    );

    @Comment("Title of the form holding one category's settings")
    private Title categoryTitle = new Title("<category> | <claim_name>", 17);

    @Comment("Show the category list even with a single category. Off opens that category directly.")
    private boolean alwaysShowCategories = false;

    @Comment("Whether each setting's description is shown above its control")
    private boolean showDescriptions = true;

    @Comment("Placeholder shown in a text field whose setting has no value yet")
    private String valueNotSet = "Not set";

    @Comment("How many settings each category holds.")
    private int perCategory = 7;

    @Comment("The category button label.")
    private String categoryName = "Settings <page>";

    private Image categoryImage = new Image();

    @Comment("Detail text shown at the top of every category form.")
    private List<String> categoryContent = List.of();

    @Comment("The label shown next to a setting's control.")
    private String label = "<white><setting_name>";

    private Navigation nav = new Navigation();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "categories",
            "extra:example-button",
            "nav",
            "back"
    );

    /**
     * A single setting row of a category form.
     */
    public record SettingRow(@NotNull Setting<?> setting, @NotNull String label, @NotNull List<String> description) {
    }

    /**
     * A group of settings, shown as one button on the category form.
     *
     * @param fallback The material used for the image when none is configured.
     */
    public record SettingCategory(
            @NotNull String name, @NotNull Image image, @Nullable Material fallback,
            @NotNull List<String> content, @NotNull List<SettingRow> settings
    ) {
    }

    /**
     * Splits the visible settings into categories, in registration order.
     */
    @NotNull
    public List<SettingCategory> categories(@NotNull ClaimSettings claimSettings) {
        List<SettingCategory> categories = new ArrayList<>();
        List<SettingRow> current = new ArrayList<>();
        Material fallback = null;

        int size = Math.max(1, perCategory);

        for (Setting<?> setting : claimSettings.visible()) {
            ClaimSettings.Entry entry = claimSettings.entry(setting);

            if (current.isEmpty()) {
                fallback = entry.icon().vanilla();
            }

            current.add(new SettingRow(
                    setting, label.replace("<setting_name>", entry.name()), entry.description()
            ));

            if (current.size() >= size) {
                categories.add(category(categories.size() + 1, current, fallback));

                current = new ArrayList<>();
            }
        }

        if (!current.isEmpty()) {
            categories.add(category(categories.size() + 1, current, fallback));
        }

        return categories;
    }

    private SettingCategory category(int page, List<SettingRow> rows, Material fallback) {
        return new SettingCategory(
                categoryName.replace("<page>", String.valueOf(page)),
                categoryImage, fallback, categoryContent, rows
        );
    }
}
