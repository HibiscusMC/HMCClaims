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
public class ClaimBannedListFormConfig extends FormTemplate {

    private Title title = new Title("Banned | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Banned players: <white><total>"
    );

    @Setting("empty-content")
    private List<String> emptyContent = List.of(
            "<gray>Nobody is banned from this claim."
    );

    @Setting("max-entries")
    @Comment("Maximum entries listed at once. Set to -1 for no limit.")
    private int maxEntries = 100;

    @Setting("more-button")
    private Button moreButton = new Button("<dark_gray>Show more...");

    @Setting("banned-entry")
    private Entry bannedEntry = new Entry(
            List.of("<name>", "<dark_gray>Banned on <banned_date>"),
            new Image()
    );

    @Setting("banned-actions")
    @Comment("Opened when a banned player is tapped")
    private SubForm bannedActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>Banned on <white><banned_date>"
            ),
            Map.of(
                    "unban", new Button("<green>Unban", Image.path("textures/ui/confirm")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    @Setting("ban-member-button")
    private Button banMemberButton = new Button("Ban a player", Image.path("textures/ui/hammer_l"));

    private Navigation nav = new Navigation();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
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