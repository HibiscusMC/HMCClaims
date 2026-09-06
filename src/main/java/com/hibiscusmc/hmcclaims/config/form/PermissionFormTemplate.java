package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
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
public class PermissionFormTemplate extends FormTemplate {

    private final static int PER_CATEGORY = 7;

    @Getter
    @ConfigSerializable
    public static class PermissionEntry {

        @Comment("The permission this row edits. Remove the row to hide the permission.")
        private Permission key;

        @Comment("The label shown next to the control")
        private String label = "";

        @Comment("Shown above the control when show-descriptions is enabled")
        private List<String> description = List.of();

        public PermissionEntry() {
        }

        public PermissionEntry(Permission key, String label, List<String> description) {
            this.key = key;
            this.label = label;
            this.description = description;
        }
    }

    /**
     * A group of permissions, shown as one button on the category form.
     */
    @Getter
    @ConfigSerializable
    public static class PermissionCategory {

        @Comment("The category button label")
        private String name = "Category";

        private Image image = new Image();

        @Comment("Detail text shown at the top of this category's form")
        private List<String> content = List.of();

        private Map<String, PermissionEntry> permissions = Map.of();

        public PermissionCategory() {
        }

        public PermissionCategory(String name, Map<String, PermissionEntry> permissions) {
            this.name = name;
            this.permissions = permissions;
        }
    }

    @NotNull
    protected static Map<Integer, PermissionCategory> buildCategories(@NotNull String namePattern) {
        Map<Integer, PermissionCategory> categories = new LinkedHashMap<>();
        Map<String, PermissionEntry> current = new LinkedHashMap<>();

        int page = 1;
        for (Permission permission : PermissionRegistry.getAllPermissions()) {
            List<String> description = Arrays.stream(("<gray>" + permission.description()).split("\n"))
                    .toList();

            current.put(key(permission), new PermissionEntry(
                    permission, "<white>" + permission.displayName(), description
            ));

            if (current.size() >= PER_CATEGORY) {
                categories.put(page, new PermissionCategory(namePattern.replace("<page>", String.valueOf(page)), current));

                current = new LinkedHashMap<>();
                page++;
            }
        }

        if (!current.isEmpty()) {
            categories.put(page, new PermissionCategory(namePattern.replace("<page>", String.valueOf(page)), current));
        }

        return categories;
    }

    @NotNull
    protected static String key(@NotNull Permission permission) {
        return RegistryUtil.serialize(permission.key()).replace(":", "-").toLowerCase() + "-permission";
    }

    @NotNull
    public static List<PermissionCategory> usable(@NotNull Map<Integer, PermissionCategory> categories) {
        List<PermissionCategory> usable = new ArrayList<>();

        categories.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    PermissionCategory category = entry.getValue();

                    boolean hasPermissions = category.permissions().values().stream()
                            .anyMatch(row -> row != null && row.key() != null);

                    if (hasPermissions) {
                        usable.add(category);
                    }
                });

        return usable;
    }

    @Setting("show-descriptions")
    @Comment("Whether each permission's description is shown above its control")
    private boolean showDescriptions = true;
}