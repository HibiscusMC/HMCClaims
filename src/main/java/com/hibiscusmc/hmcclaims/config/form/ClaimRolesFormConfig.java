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
public class ClaimRolesFormConfig extends FormTemplate {

    private Title title = new Title("Roles | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Roles are ordered from most to least powerful."
    );

    @Setting("role-entry")
    private Entry roleEntry = new Entry(
            List.of("<name>", "<gray><members> member(s)"),
            Image.path("textures/ui/permissions_op_crown")
    );

    @Setting("role-actions")
    @Comment("""
            Opened when a role is tapped. The inventory GUI binds these to left-click,
            right-click and shift-click; on Bedrock they each get their own button.
            Actions the viewer lacks permission for are hidden.""")
    private SubForm roleActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>Members: <white><members>",
                    "<gray>Created: <white><creation_date>"
            ),
            Map.of(
                    "permissions", new Button("Edit permissions", Image.path("textures/ui/gear")),
                    "rename", new Button("Rename", Image.path("textures/items/book_writable")),
                    "move-up", new Button("Move up", Image.path("textures/ui/arrow_up")),
                    "move-down", new Button("Move down", Image.path("textures/ui/arrow_down")),
                    "delete", new Button("<red>Delete role", Image.path("textures/ui/redX1")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    @Setting("create-role-button")
    private Button createRoleButton = new Button("Create role", Image.path("textures/ui/color_plus"));

    @Setting("cant-create")
    private String cantCreate = "<prefix><red>You can't create roles in this claim.";

    private Navigation nav = new Navigation();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
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