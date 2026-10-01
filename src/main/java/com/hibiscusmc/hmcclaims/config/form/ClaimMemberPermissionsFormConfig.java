package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimMemberPermissionsFormConfig extends PermissionFormTemplate {

    private Title title = new Title("Permissions | <member_name>", 20);

    private List<String> content = List.of(
            "<gray>Pick a group of permissions to edit.",
            "<gray>Overrides here win over the member's role."
    );

    @Comment("Title of the form holding one category's permissions")
    private Title categoryTitle = new Title("<category> | <member_name>", 20);

    @Comment("Shown at the top of a category the viewer can't change")
    private List<String> readOnlyContent = List.of(
            "<red>You can't change this member's permissions."
    );

    @Comment("Bedrock has no disabled dropdown, so a permission the viewer can't change becomes a read-only line.")
    private String readOnlyRow = "<label>: <value>";

    @Comment("Labels for the three states. Unset means the member inherits the value from their role.")
    private Map<String, String> states = MapUtil.ordered(
            "enabled", "Allowed",
            "unset", "Inherit from role",
            "disabled", "Denied"
    );

    private Tabs tabs = new Tabs();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
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
    @Section
    public static class Tabs {

        private Button roleButton = new Button("Change role", Image.path("textures/ui/permissions_op_crown"));

        private Button memberListButton = new Button("All members", Image.path("textures/ui/FriendsIcon"));
    }
}