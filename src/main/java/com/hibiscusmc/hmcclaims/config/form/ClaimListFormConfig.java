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
public class ClaimListFormConfig extends FormTemplate {

    private String title = "Your claims";

    @Comment("Text shown above the buttons")
    private List<String> content = List.of(
            "<gray>Filter: <white><filter>",
            "<gray>Search: <white><query>",
            "",
            "<gray>Showing <white><shown></white> of <white><total></white> claims."
    );

    @Comment("Replaces the text above when the player has no claims to show")
    private List<String> emptyContent = List.of(
            "<gray>You don't have any claims here yet."
    );

    @Comment("Shown for <query> while no search is active")
    private String noQuery = "Nothing";

    @Comment("Maximum claims listed at once. Set to -1 for no limit.")
    private int maxEntries = 100;

    @Comment("Shown when the list was cut short by max-entries")
    private Button moreButton = new Button("<dark_gray>Show more...");

    private Button searchButton = new Button("Search & Filter", Image.path("textures/items/spyglass"));

    private Entry claimEntry = new Entry(
            List.of("<name>", "<dark_gray><world> · <surface_area> blocks"),
            Image.path("textures/blocks/grass_side_carried")
    );

    private Entry subClaimEntry = new Entry(
            List.of("<name>", "<dark_gray>in <main_claim>"),
            Image.path("textures/blocks/dirt")
    );

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
            MapUtil.ordered(
                    "manage", new Button("Manage claim", Image.path("textures/ui/hammer_l")),
                    "rename", new Button("Rename", Image.path("textures/items/book_writable")),
                    "back", new Button("Back", Image.path("textures/ui/arrow_left"))
            )
    );

    @Comment("The search and filter form opened by the search button")
    private Search search = new Search();

    @Comment("Only rendered when there is a previous form to return to")
    private Button backButton = new Button("Back", Image.path("textures/ui/arrow_left"));

    @Comment("Custom buttons. Place them with \"extra:<key>\" in the order list.")
    private Map<String, ActionButton> extraButtons = MapUtil.ordered(
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
    @Section
    public static class Search {

        private String title = "Search & Filter";

        private Input query = new Input("<white>Search", "Claim name, id, member...", 40);

        @Comment("Which field the query is matched against")
        private Dropdown searchBy = new Dropdown("<white>Search by", MapUtil.ordered(
                "name", "Claim name",
                "id", "Claim id",
                "main", "Main claim name",
                "member", "Member name"
        ));

        private Dropdown filter = new Dropdown("<white>Show", MapUtil.ordered(
                "ALL", "All claims",
                "MAIN", "Main claims",
                "SUB_CLAIMS", "Sub claims"
        ));
    }
}