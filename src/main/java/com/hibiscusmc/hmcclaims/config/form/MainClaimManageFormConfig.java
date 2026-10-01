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
public class MainClaimManageFormConfig extends ClaimManageFormConfig {

    private Title title = new Title("Manage | <claim_name>", 19);

    private List<String> content = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Private: <white><locked>",
            "<gray>Location: <white><world>, X: <x>, Z: <z>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
            "<gray>Sub Claims: <white><total_sub_claims>"
    );

    private Button renameButton = new Button("Rename claim", Image.path("textures/items/book_writable"));

    @Comment("Shown while the claim is open to everyone")
    private Button lockButton = new Button("Lock claim", Image.path("textures/items/door_iron"));

    @Comment("Shown while the claim is locked")
    private Button unlockButton = new Button("Unlock claim", Image.path("textures/blocks/door_wood_upper"));

    private Button bannedButton = new Button("Banned players", Image.path("textures/ui/hammer_l"));

    private Button transferButton = new Button("Transfer ownership", Image.path("textures/items/nether_star"));

    @Comment("Closes the form and puts the player into resize mode")
    private Button resizeButton = new Button("Resize claim", Image.path("textures/items/gold_shovel"));

    private Button deleteButton = new Button("<red>Delete claim", Image.path("textures/ui/redX1"));

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
            "transfer",
            "resize",
            "delete",
            "extra:example-button",
            "nav",
            "back"
    );
}