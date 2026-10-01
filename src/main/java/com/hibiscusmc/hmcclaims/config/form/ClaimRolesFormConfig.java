package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;

import java.util.List;
import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimRolesFormConfig extends FormTemplate {

    private Title title = new Title("Roles | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Roles are ordered from most to least powerful."
    );

    private Entry roleEntry = new Entry(
            List.of("<name>", "<gray><members> member(s)"),
            Image.path("textures/ui/permissions_op_crown")
    );

    @Comment("Opened when a role is tapped. Actions the viewer lacks permission for are hidden.")
    private SubForm roleActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>Members: <white><members>",
                    "<gray>Created: <white><creation_date>"
            ),
            MapUtil.ordered(
                    "permissions", new Button("Edit permissions", Image.path("textures/ui/gear")),
                    "rename", new Button("Rename", Image.path("textures/items/book_writable")),
                    "move-up", new Button("Move up", Image.path("textures/ui/arrow_up")),
                    "move-down", new Button("Move down", Image.path("textures/ui/arrow_down")),
                    "delete", new Button("<red>Delete role", Image.path("textures/ui/redX1")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    private Button createRoleButton = new Button("Create role", Image.path("textures/ui/color_plus"));

    private String cantCreate = "<prefix><red>You can't create roles in this claim.";

    private Navigation nav = new Navigation();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "create-role",
            "roles",
            "extra:example-button",
            "nav",
            "back"
    );
}