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
public class SubClaimManageFormConfig extends ClaimManageFormConfig {

    private Title title = new Title("Manage | <claim_name>", 19);

    private List<String> content = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Main claim: <white><main_claim>",
            "<gray>Private: <white><locked>",
            "<gray>Location: <white><world>, X: <x>, Z: <z>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)"
    );

    @Setting("rename-button")
    private Button renameButton = new Button("Rename sub claim", Image.path("textures/items/book_writable"));

    @Setting("lock-button")
    @Comment("Shown while the sub claim is open to everyone")
    private Button lockButton = new Button("Lock sub claim", Image.path("textures/items/door_iron"));

    @Setting("unlock-button")
    @Comment("Shown while the sub claim is locked")
    private Button unlockButton = new Button("Unlock sub claim", Image.path("textures/blocks/door_wood_upper"));

    @Setting("banned-button")
    private Button bannedButton = new Button("Banned players", Image.path("textures/ui/hammer_l"));

    @Setting("inherit-button")
    @Comment("Copies the main claim's members, roles and permissions onto this sub claim")
    private Button inheritButton = new Button("Inherit permissions", Image.path("textures/ui/copy"));

    @Setting("inherit-confirm")
    @Comment("Confirmation shown before the inherited permissions overwrite this sub claim")
    private Confirm inheritConfirm = new Confirm();

    @Setting("inherit-success")
    private String inheritSuccess = "<prefix><green>Permissions inherited from <white><main_claim></white>!";

    @Setting("resize-button")
    @Comment("Closes the form and puts the player into resize mode")
    private Button resizeButton = new Button("Resize sub claim", Image.path("textures/items/gold_shovel"));

    @Setting("delete-button")
    private Button deleteButton = new Button("<red>Delete sub claim", Image.path("textures/ui/redX1"));

    private Navigation nav = new Navigation();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
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
    @ConfigSerializable
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