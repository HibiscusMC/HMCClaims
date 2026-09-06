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
public class MainClaimManageFormConfig extends ClaimManageFormConfig {

    private Title title = new Title("Manage | <claim_name>", 19);

    private List<String> content = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Private: <white><locked>",
            "<gray>Location: <white><world>, X: <x>, Z: <z>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
            "<gray>Sub Claims: <white><total_sub_claims>"
    );

    @Setting("rename-button")
    private Button renameButton = new Button("Rename claim", Image.path("textures/items/book_writable"));

    @Setting("lock-button")
    @Comment("Shown while the claim is open to everyone")
    private Button lockButton = new Button("Lock claim", Image.path("textures/items/door_iron"));

    @Setting("unlock-button")
    @Comment("Shown while the claim is locked")
    private Button unlockButton = new Button("Unlock claim", Image.path("textures/blocks/door_wood_upper"));

    @Setting("banned-button")
    private Button bannedButton = new Button("Banned players", Image.path("textures/ui/hammer_l"));

    @Setting("transfer-button")
    private Button transferButton = new Button("Transfer ownership", Image.path("textures/items/nether_star"));

    @Setting("resize-button")
    @Comment("Closes the form and puts the player into resize mode")
    private Button resizeButton = new Button("Resize claim", Image.path("textures/items/gold_shovel"));

    @Setting("delete-button")
    private Button deleteButton = new Button("<red>Delete claim", Image.path("textures/ui/redX1"));

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
            "transfer",
            "resize",
            "delete",
            "extra:example-button",
            "nav",
            "back"
    );
}