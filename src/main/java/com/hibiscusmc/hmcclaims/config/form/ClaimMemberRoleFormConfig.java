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
public class ClaimMemberRoleFormConfig extends FormTemplate {

    private Title title = new Title("Role | <member_name>", 20);

    private List<String> content = List.of(
            "<gray>Current role: <white><role>",
            "",
            "<gray>Tap a role to assign it."
    );

    @Comment("A role the viewer can assign to this member")
    private Entry roleEntry = new Entry(
            List.of("<name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/permissions_op_crown")
    );

    @Comment("The role the member already has")
    private Entry selectedRoleEntry = new Entry(
            List.of("<dark_green><name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/confirm")
    );

    @Comment("A role the viewer can't assign, either through rank or missing permissions")
    private Entry unavailableRoleEntry = new Entry(
            List.of("<dark_gray><name>", "<dark_gray><members> member(s)"),
            Image.path("textures/ui/Ping_Offline_Red_Dark")
    );

    private String cantAssign = "<prefix><red>You can't give this member that role.";

    private Tabs tabs = new Tabs();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
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
    @Section
    public static class Tabs {

        private Button permissionsButton = new Button("Permissions", Image.path("textures/ui/gear"));

        private Button memberListButton = new Button("All members", Image.path("textures/ui/FriendsIcon"));
    }
}