package com.hibiscusmc.hmcclaims.config.form;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimRoleManageFormConfig extends PermissionFormTemplate {

    private Title title = new Title("Permissions | <role_name>", 20);

    private List<String> content = List.of(
            "<gray>Pick a group of permissions to edit."
    );

    @Setting("category-title")
    @Comment("Title of the form holding one category's permissions")
    private Title categoryTitle = new Title("<category> | <role_name>", 20);

    @Setting("read-only-content")
    @Comment("Shown at the top of a category the viewer can't change")
    private List<String> readOnlyContent = List.of(
            "<red>You can't change these permissions."
    );

    @Setting("read-only-row")
    @Comment("""
            How a permission is rendered when the viewer can't change it. Bedrock has no
            disabled toggle, so it becomes a read-only line instead.""")
    private String readOnlyRow = "<label>: <value>";

    @Comment("Labels for the two permission states")
    private Map<String, String> states = Map.of(
            "enabled", "<green>Enabled",
            "disabled", "<red>Disabled"
    );

    @Setting("permission-categories")
    private Map<Integer, PermissionCategory> permissionCategories = buildCategories("Page <page>");

    @Setting("delete-button")
    private Button deleteButton = new Button("<red>Delete role", Image.path("textures/ui/redX1"));

    @Setting("cant-delete")
    private String cantDelete = "<prefix><red>You can't delete this role.";

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
            "delete",
            "extra:example-button",
            "nav",
            "back"
    );
}