package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Permissions;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Section;

import java.util.ArrayList;
import java.util.List;

@Getter
@Section
@SuppressWarnings({"FieldMayBeFinal"})
public class PermissionFormTemplate extends FormTemplate {

    @Comment("How many permissions each category holds.")
    private int perCategory = 7;

    @Comment("The category button label.")
    private String categoryName = "Page <page>";

    private Image categoryImage = new Image();

    @Comment("Detail text shown at the top of every category form.")
    private List<String> categoryContent = List.of();

    @Comment("The label shown next to a permission's control.")
    private String label = "<white><permission_name>";

    @Comment("Whether each permission's description is shown above its control")
    private boolean showDescriptions = true;

    /**
     * A single permission row of a category form.
     */
    public record PermissionRow(@NotNull Permission permission, @NotNull String label,
                                @NotNull List<String> description) {
    }

    /**
     * A group of permissions, shown as one button on the category form.
     *
     * @param fallback The material used for the image when none is configured.
     */
    public record PermissionCategory(
            @NotNull String name, @NotNull Image image, @Nullable Material fallback,
            @NotNull List<String> content, @NotNull List<PermissionRow> permissions
    ) {
    }

    /**
     * Splits the visible permissions into categories, in registration order.
     */
    @NotNull
    public List<PermissionCategory> categories(@NotNull Permissions permissions) {
        List<PermissionCategory> categories = new ArrayList<>();
        List<PermissionRow> current = new ArrayList<>();
        Material fallback = null;

        int size = Math.max(1, perCategory);

        for (Permission permission : permissions.visible()) {
            Permissions.Entry entry = permissions.entry(permission);

            if (current.isEmpty()) {
                fallback = entry.icon().vanilla();
            }

            current.add(new PermissionRow(
                    permission, label.replace("<permission_name>", entry.name()), entry.description()
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

    private PermissionCategory category(int page, List<PermissionRow> rows, Material fallback) {
        return new PermissionCategory(
                categoryName.replace("<page>", String.valueOf(page)),
                categoryImage, fallback, categoryContent, rows
        );
    }
}