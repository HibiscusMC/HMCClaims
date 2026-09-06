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
public class ClaimMemberListFormConfig extends FormTemplate {

    private Title title = new Title("Members | <claim_name>", 17);

    private List<String> content = List.of(
            "<gray>Filter: <white><filter>",
            "<gray>Search: <white><query>",
            "",
            "<gray>Showing <white><shown></white> of <white><total></white> members."
    );

    @Setting("empty-content")
    private List<String> emptyContent = List.of(
            "<gray>No members match your filters."
    );

    @Setting("no-query")
    private String noQuery = "Nothing";

    @Setting("max-entries")
    @Comment("Maximum members listed at once. Set to -1 for no limit.")
    private int maxEntries = 100;

    @Setting("more-button")
    private Button moreButton = new Button("<gray>Show more...");

    @Setting("member-entry")
    @Comment("A member the viewer is allowed to manage")
    private Entry memberEntry = new Entry(
            List.of("<dark_gray><name>", "<dark_gray><role>"),
            new Image(ImageType.URL, "https://mineskin.eu/helm/<clean_uuid>/100.png")
    );

    @Setting("unmanageable-member-entry")
    @Comment("A member the viewer outranks, or cannot manage")
    private Entry unmanageableMemberEntry = new Entry(
            List.of("<red><name>", "<dark_gray><role>"),
            Image.path("textures/ui/Ping_Offline_Red_Dark")
    );

    @Setting("member-actions")
    @Comment("""
            Opened when a member is tapped. This is the Bedrock equivalent of the member
            screen's lower inventory: entries the viewer lacks permission for are hidden.""")
    private SubForm memberActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>Role: <white><role>",
                    "<gray>Joined: <white><joined_date>"
            ),
            Map.of(
                    "role", new Button("Change role", Image.path("textures/ui/permissions_op_crown")),
                    "permissions", new Button("Permissions", Image.path("textures/ui/gear")),
                    "kick", new Button("<red>Kick", Image.path("textures/ui/redX1")),
                    "ban", new Button("<dark_red>Ban", Image.path("textures/ui/hammer_l")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    @Setting("cant-kick")
    @Comment("Sent when the viewer taps Kick without the permission to do so")
    private String cantKick = "<prefix><red>You can't kick this member.";

    @Setting("cant-ban")
    private String cantBan = "<prefix><red>You can't ban this member.";

    @Setting("search-button")
    private Button searchButton = new Button("Search & Filter", Image.path("textures/items/spyglass"));

    @Comment("The search and filter form opened by the search button")
    private Search search = new Search();

    @Setting("add-member-button")
    private Button addMemberButton = new Button("Add member", Image.path("textures/ui/color_plus"));

    private Navigation nav = new Navigation();

    @Setting("back-button")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    private Map<String, ActionButton> extraButtons = Map.of(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "add-member",
            "search",
            "members",
            "extra:example-button",
            "nav",
            "back"
    );

    @Getter
    @ConfigSerializable
    public static class Search {

        private String title = "Search & Filter";

        private Input query = new Input("<white>Member name", "Type a name...", 40);

        @Comment("""
                Roles are appended to this dropdown automatically.
                <role> is replaced with the role name.""")
        private Dropdown filter = new Dropdown("<white>Role", Map.of(
                "ALL", "All roles",
                "ROLE", "<role>"
        ));
    }
}