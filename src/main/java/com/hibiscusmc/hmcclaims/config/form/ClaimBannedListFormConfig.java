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
public class ClaimBannedListFormConfig extends FormTemplate {

    private Title title = new Title("Banned | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Banned players: <white><total>"
    );

    private List<String> emptyContent = List.of(
            "<gray>Nobody is banned from this claim."
    );

    @Comment("Maximum entries listed at once. Set to -1 for no limit.")
    private int maxEntries = 100;

    private Button moreButton = new Button("<dark_gray>Show more...");

    private Entry bannedEntry = new Entry(
            List.of("<name>", "<dark_gray>Banned on <banned_date>"),
            new Image()
    );

    @Comment("Opened when a banned player is tapped")
    private SubForm bannedActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>Banned on <white><banned_date>"
            ),
            MapUtil.ordered(
                    "unban", new Button("<green>Unban", Image.path("textures/ui/confirm")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    private Button banMemberButton = new Button("Ban a player", Image.path("textures/ui/hammer_l"));

    private Navigation nav = new Navigation();

    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "ban-member",
            "banned",
            "extra:example-button",
            "nav",
            "back"
    );
}