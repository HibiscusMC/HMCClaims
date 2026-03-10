package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@Getter
@ToString
@ConfigSerializable
public class Messages {

    private String prefix = "<b><gradient:#49fc4f:#3ffcb4:#49fc4f>HMCClaims</gradient> <dark_gray>|</dark_gray></b> ";

    @Setting("reload")
    private String pluginReload = "<gray>Plugin reloaded!";

    private Claims claims = new Claims();

    private Commands commands = new Commands();

    private Dialogs dialogs = new Dialogs();

    private Inputs inputs = new Inputs();

    @Getter
    @ToString
    @ConfigSerializable
    public static class Claims {

        @Setting("owned-by")
        private String ownedBy = "<gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        private String created = "<gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

        @Setting("sub-claim-created")
        private String subCreated = "<gray>The sub claim <#d24c9f><name> <gray>has been created!";

        private Selecting selecting = new Selecting();

        @Getter
        @ToString
        @ConfigSerializable
        public static class Selecting {

            @Setting("first-selection")
            private String firstSelection = "<gray>Selected <#d24c9f>first corner <gray>at location <#d24c9f><location><gray>.";

            @Setting("second-selection")
            private String secondSelection = "<gray>Selected <#d24c9f>second corner <gray>at location <#d24c9f><location><gray>. <#d24c9f>Left-Click <gray>to create your claim!";

            @Setting("selection-removed")
            private String selectionRemoved = "<gray>Your selection has been removed!";

            @Setting("corner-unselected")
            private String cornerUnselected = "<gray>Corner at location <#d24c9f><location> <gray>has been unselected.";

            @Setting("must-select-region")
            private String mustSelectRegion = "<red>You must select a region with <#d24c9f>Left-Click<red>!";

            @Setting("must-select-points")
            private String mustSelectPoints = "<red>You must select two points!";

            @Setting("claim-overlaps")
            private String claimOverlaps = "<red>There's already a claim on this area!";

            @Setting("selection-too-small")
            private String selectionTooSmall = "<red>The selected region is too small! <gray>(Should be at least 5x5)";

            @Setting("not-enough-claimblocks")
            private String notEnoughClaimBlocks = "<red>You don't have enough claim blocks! <gray>Required: <white><required_blocks></white>, <gray>Current: <white><current_blocks>";

            @Setting("land-already-claimed")
            private String landAlreadyClaimed = "<red>Someone already claimed this land!";

            @Setting("claim-within-sub")
            private String claimWithinSub = "<red>Can't create a claim within a sub claim";

            @Setting("sub-outside-boundaries")
            private String subOutsideBoundaries = "<red>Sub claim can't be outside of main claim boundaries";
        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class Commands {

        @Setting("no-permission")
        private String noPermission = "<red>No permissions.";

        private String usage = "<gray>Command usage: <white>/<command> <usage>";

        @Setting("invalid-argument")
        private String invalidArgument = "<red>Invalid argument provided!";

        @Setting("missing-player")
        private String missingPlayer = "<red>You need to specify a player!";

        @Setting("player-not-found")
        private String playerNotFound = "<red>Player not found";

        @Setting("not-in-claim")
        private String notInClaim = "<red>You're not standing in a claim!";

        @Setting("claim-blocks")
        private ClaimBlocks claimBlocks = new ClaimBlocks();

        private Claim claim = new Claim();

        @Getter
        @ToString
        @ConfigSerializable
        public static class Claim {

            private String add = "<green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!";

            @Setting("already-added")
            private String alreadyAdded = "<red>This player is already in the claim!";
        }

        @Getter
        @ToString
        @ConfigSerializable
        public static class ClaimBlocks {

            private String summary = """
                    <gray><white><player_name></white>'s claim blocks breakdown:
                    
                    <dark_gray><b>»</b> <gray>Starting Blocks: <white><starting_blocks>
                    <dark_gray><b>»</b> <gray>Obtained Blocks: <white><obtained_blocks>
                    <dark_gray><b>»</b> <gray>Total Blocks: <white><total_blocks>
                    
                    <dark_gray><b>»</b> <gray>Used Blocks: <white><used_blocks>
                    <dark_gray><b>»</b> <gray>Available Blocks: <white><available_blocks>""";
        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class Dialogs {

        private Search search = new Search();

        private Rename rename = new Rename();

        @Getter
        @ToString
        @ConfigSerializable
        public static class Search {

            private String title = "Search";

            private String query = "Query";

            @Setting("option-title")
            private String optionTitle = "Search by";

            private Map<String, String> options = Map.of(
                    "name", "Claim Name",
                    "id", "Claim Id",
                    "main", "Main Claim Name",
                    "member", "Member Name"
            );

            private Map<String, Button> buttons = Map.of(
                    "submit", new Button(
                            "Search", "Click to search"
                    ),
                    "cancel", new Button(
                            "Cancel", "Click to cancel"
                    )
            );
        }

        @Getter
        @ToString
        @ConfigSerializable
        public static class Rename {

            private String title = "Rename Your Claim";

            private String input = "Input the new name";

            private Map<String, Button> buttons = Map.of(
                    "submit", new Button(
                            "Confirm", "Click to rename your claim"
                    ),
                    "cancel", new Button(
                            "Cancel", "Click to cancel"
                    )
            );
        }

        @Getter
        @ToString
        @ConfigSerializable
        public static class Button {

            private String label = "Button Label";

            private String tooltip = "Button Tooltip";

            public Button() {

            }

            public Button(String label, String tooltip) {
                this.label = label;
                this.tooltip = tooltip;
            }

        }
    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class Inputs {

        private String cancelled = "<red>Input cancelled.";

        private Title title = new Title();

        @Setting("action-bar")
        private ActionBar actionBar = new ActionBar();

        @Getter
        @ToString
        @ConfigSerializable
        public static class Title {

            private boolean enabled = true;

            private String title = "<yellow>Enter Input";

            private String subtitle = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }

        @Getter
        @ToString
        @ConfigSerializable
        public static class ActionBar {

            private boolean enabled = true;

            private String text = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }
    }
}