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
public class ClaimMemberPermissionsFormConfig extends PermissionFormTemplate {

    private Title title = new Title("Permissions | <member_name>", 20);

    private List<String> content = List.of(
            "<gray>Pick a group of permissions to edit.",
            "<gray>Overrides here win over the member's role."
    );

    @Setting("category-title")
    @Comment("Title of the form holding one category's permissions")
    private Title categoryTitle = new Title("<category> | <member_name>", 20);

    @Setting("read-only-content")
    @Comment("Shown at the top of a category the viewer can't change")
    private List<String> readOnlyContent = List.of(
            "<red>You can't change this member's permissions."
    );

    @Setting("read-only-row")
    @Comment("""
            How a permission is rendered when the viewer can't change it. Bedrock has no
            disabled dropdown, so it becomes a read-only line instead.""")
    private String readOnlyRow = "<label>: <value>";

    @Comment("""
            Labels for the three permission states, in the order they appear in the
            dropdown. "unset" means the member inherits the value from their role.""")
    private Map<String, String> states = Map.of(
            "enabled", "Allowed",
            "unset", "Inherit from role",
            "disabled", "Denied"
    );

    @Setting("permission-categories")
    private Map<Integer, PermissionCategory> permissionCategories = buildCategories("Page <page>");

    private Tabs tabs = new Tabs();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "categories",
            "tabs",
            "extra:example-button",
            "back"
    );

    @Getter
    @ConfigSerializable
    public static class Tabs {

        @Setting("role-button")
        private Button roleButton = new Button("Change role", Image.path("textures/ui/permissions_op_crown"));

        @Setting("member-list-button")
        private Button memberListButton = new Button("All members", Image.path("textures/ui/FriendsIcon"));
    }
}