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
public class ClaimMemberRoleFormConfig extends FormTemplate {

    private Title title = new Title("Role | <member_name>", 20);

    private List<String> content = List.of(
            "<gray>Current role: <white><role>",
            "",
            "<gray>Tap a role to assign it."
    );

    @Setting("role-entry")
    @Comment("A role the viewer can assign to this member")
    private Entry roleEntry = new Entry(
            List.of("<name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/permissions_op_crown")
    );

    @Setting("selected-role-entry")
    @Comment("The role the member already has")
    private Entry selectedRoleEntry = new Entry(
            List.of("<dark_green><name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/confirm")
    );

    @Setting("unavailable-role-entry")
    @Comment("A role the viewer can't assign, either through rank or missing permissions")
    private Entry unavailableRoleEntry = new Entry(
            List.of("<dark_gray><name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/Ping_Offline_Red_Dark")
    );

    @Setting("cant-assign")
    private String cantAssign = "<prefix><red>You can't give this member that role.";

    private Tabs tabs = new Tabs();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "roles",
            "tabs",
            "extra:example-button",
            "back"
    );

    @Getter
    @ConfigSerializable
    public static class Tabs {

        @Setting("permissions-button")
        private Button permissionsButton = new Button("Permissions", Image.path("textures/ui/gear"));

        @Setting("member-list-button")
        private Button memberListButton = new Button("All members", Image.path("textures/ui/FriendsIcon"));
    }
}