package com.hibiscusmc.hmcclaims.config.form;

import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Config
@Getter
@SuppressWarnings({"FieldMayBeFinal"})
public class SubClaimManageFormConfig extends ClaimManageFormConfig {

    private Title title = new Title("Manage | <claim_name>", 19);

    private List<String> content = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Main claim: <white><main_claim>",
            "<gray>Private: <white><locked>",
            "<gray>Location: <white><world>, X: <x>, Z: <z>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)"
    );

    private Button renameButton = new Button("Rename sub claim", Image.path("textures/items/book_writable"));

    @Comment("Shown while the sub claim is open to everyone")
    private Button lockButton = new Button("Lock sub claim", Image.path("textures/items/door_iron"));

    @Comment("Shown while the sub claim is locked")
    private Button unlockButton = new Button("Unlock sub claim", Image.path("textures/blocks/door_wood_upper"));

    private Button bannedButton = new Button("Banned players", Image.path("textures/ui/hammer_l"));

    @Comment("Copies the main claim's members, roles and permissions onto this sub claim")
    private Button inheritButton = new Button("Inherit permissions", Image.path("textures/ui/copy"));

    @Comment("Confirmation shown before the inherited permissions overwrite this sub claim")
    private Confirm inheritConfirm = new Confirm();

    private String inheritSuccess = "<prefix><green>Permissions inherited from <white><main_claim></white>!";

    @Comment("Closes the form and puts the player into resize mode")
    private Button resizeButton = new Button("Resize sub claim", Image.path("textures/items/gold_shovel"));

    private Button deleteButton = new Button("<red>Delete sub claim", Image.path("textures/ui/redX1"));

    private Navigation nav = new Navigation();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "rename",
            "lock",
            "banned",
            "inherit",
            "resize",
            "delete",
            "extra:example-button",
            "nav",
            "back"
    );

    @Getter
    @Section
    public static class Confirm {

        private String title = "Inherit permissions?";

        private List<String> content = List.of(
                "<gray>This replaces this sub claim's members, roles and",
                "<gray>permissions with the ones from <white><main_claim></white>.",
                "",
                "<red>This cannot be undone."
        );

        private String confirm = "<green>Inherit";

        private String cancel = "Cancel";
    }
}