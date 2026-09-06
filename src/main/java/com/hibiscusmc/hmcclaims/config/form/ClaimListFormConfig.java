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
public class ClaimListFormConfig extends FormTemplate {

    private String title = "Your claims";

    @Comment("Text shown above the buttons")
    private List<String> content = List.of(
            "<gray>Filter: <white><filter>",
            "<gray>Search: <white><query>",
            "",
            "<gray>Showing <white><shown></white> of <white><total></white> claims."
    );

    @Setting("empty-content")
    @Comment("Replaces the text above when the player has no claims to show")
    private List<String> emptyContent = List.of(
            "<gray>You don't have any claims here yet."
    );

    @Setting("no-query")
    @Comment("Shown for <query> while no search is active")
    private String noQuery = "Nothing";

    @Setting("max-entries")
    @Comment("""
            Maximum claims listed at once. Bedrock scrolls fine, but a very long list is
            slow to page through on a phone. Set to -1 for no limit.""")
    private int maxEntries = 100;

    @Setting("more-button")
    @Comment("Shown when the list was cut short by max-entries")
    private Button moreButton = new Button("<dark_gray>Show more...");

    @Setting("search-button")
    private Button searchButton = new Button("Search & Filter", Image.path("textures/items/spyglass"));

    @Setting("claim-entry")
    private Entry claimEntry = new Entry(
            List.of("<name>", "<dark_gray><world> · <surface_area> blocks"),
            Image.path("textures/blocks/grass_side_carried")
    );

    @Setting("sub-claim-entry")
    private Entry subClaimEntry = new Entry(
            List.of("<name>", "<dark_gray>in <main_claim>"),
            Image.path("textures/blocks/dirt")
    );

    @Setting("claim-actions")
    @Comment("Opened when a claim is tapped")
    private SubForm claimActions = new SubForm(
            new Title("<name>", 28),
            List.of(
                    "<gray>UID: <white><short_id>",
                    "<gray>Private: <white><locked>",
                    "<gray>Location: <white><world>, X: <x>, Z: <z>",
                    "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
                    "<gray>Sub Claims: <white><total_sub_claims>",
                    "<gray>Members: <white><member_count>",
                    "<gray>Created: <white><creation_date>"
            ),
            Map.of(
                    "manage", new Button("Manage claim", Image.path("textures/ui/hammer_l")),
                    "rename", new Button("Rename", Image.path("textures/items/book_writable")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    @Comment("The search and filter form opened by the search button")
    private Search search = new Search();

    @Setting("back-button")
    @Comment("Only rendered when there is a previous form to return to")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Setting("extra-buttons")
    @Comment("Custom buttons. Place them with \"extra:<key>\" in the order list.")
    private Map<String, ActionButton> extraButtons = Map.of(
            "example-button", new ActionButton("Example Button")
    );

    @Comment(ORDER_DESCRIPTION)
    private List<String> order = List.of(
            "search",
            "claims",
            "extra:example-button",
            "back"
    );

    @Getter
    @ConfigSerializable
    public static class Search {

        private String title = "Search & Filter";

        private Input query = new Input("<white>Search", "Claim name, id, member...", 40);

        @Setting("search-by")
        @Comment("Which field the query is matched against")
        private Dropdown searchBy = new Dropdown("<white>Search by", Map.of(
                "name", "Claim name",
                "id", "Claim id",
                "main", "Main claim name",
                "member", "Member name"
        ));

        private Dropdown filter = new Dropdown("<white>Show", Map.of(
                "ALL", "All claims",
                "MAIN", "Main claims",
                "SUB_CLAIMS", "Sub claims"
        ));
    }
}