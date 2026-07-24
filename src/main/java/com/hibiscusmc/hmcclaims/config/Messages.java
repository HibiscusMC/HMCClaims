package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class Messages {

    private String prefix = "<b><gradient:#49fc4f:#3ffcb4:#49fc4f>HMCClaims</gradient> <dark_gray>|</dark_gray></b> ";

    @Setting("reload")
    private String pluginReload = "<gray>Plugin reloaded!";

    private Claims claims = new Claims();

    private Commands commands = new Commands();

    private Dialogs dialogs = new Dialogs();

    private Inputs inputs = new Inputs();

    @Getter
    @ConfigSerializable
    public static class Claims {

        @Setting("owned-by")
        private String ownedBy = "<gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        private String created = "<gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

        private String resized = "<gray>The claim <#d24c9f><name> <gray>has been resized! Price: <#d24c9f><price>";

        @Setting("sub-claim-created")
        private String subCreated = "<gray>The sub claim <#d24c9f><name> <gray>has been created!";

        @Setting("sub-claim-resized")
        private String subResized = "<gray>The sub claim <#d24c9f><name> <gray>has been resized!";

        @Setting("member-added")
        private String memberAdded = "<green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!";

        @Setting("member-remove")
        private String memberRemoved = "<red>Player <white><player_head> <name></white> <red>removed from <white><claim></white>!";

        @Setting("member-already-added")
        private String memberAlreadyAdded = "<red>This player is already in the claim!";

        @Setting("member-already-banned")
        private String memberAlreadyBanned = "<red>This player is already banned!";

        @Setting("cant-ban-member")
        private String cantBanMember = "<red>You can't ban this member!";

        @Setting("player-not-member")
        private String playerNotMember = "<red>This player is not a member of this claim!";

        @Setting("self-already-owner")
        private String selfAlreadyOwner = "<red>You're already the owner of this claim!";

        @Setting("member-already-owner")
        private String memberAlreadyOwner = "<red>This member is already the owner of this claim!";

        @Setting("claim-transferred")
        private String claimTransferred = "<green>Claim <white><claim></white> transferred to <white><player_head> <name></white>!";

        @Setting("member-banned")
        private String memberBanned = "<green>Player <white><player_head> <name></white> was banned from the claim <white><claim></white>!";

        private Selecting selecting = new Selecting();

        private Resizing resizing = new Resizing();

        @Getter
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

            @Setting("resizing-wrong-claim")
            private String resizingWrongClaim = "<red>You are not resizing this claim!";

            @Setting("claim-within-sub")
            private String claimWithinSub = "<red>Can't create a claim within a sub claim";

            @Setting("sub-outside-boundaries")
            private String subOutsideBoundaries = "<red>Sub claim can't be outside of main claim boundaries";
        }

        @Getter
        @ConfigSerializable
        public static class Resizing {

            @Setting("select-a-corner")
            private String selectACorner = "<red>You should select a corner";

            @Setting("enclose-claim-boundaries")
            private String encloseClaimBoundaries = "<red>The new region must completely enclose the original claim boundaries!";

            @Setting("within-main-claim")
            private String withinMainClaim = "<red>The new region must be inside of the main claim boundaries!";

            @Setting("sub-within-sub")
            private String subWithinSub = "<red>There's already a sub claim here!";
        }
    }

    @Getter
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

        @Setting("not-your-claim")
        private String notYourClaim = "<red>This is not your claim!";

        @Setting("not-in-claim")
        private String notInClaim = "<red>You're not standing in a claim!";

        @Setting("delete-confirm")
        private String deleteConfirm = "<red><b>DANGER!</b> <gray>Are you sure you want to delete <white><claim_name></white>?<br>  <red><click:run_command:'/claim delete confirm;<claim_id>'>[Yes! Delete claim]</click>";

        @Setting("delete-success")
        private String deleteSuccess = "<white>Claim deleted successfully!";

        @Setting("claim-blocks")
        private ClaimBlocks claimBlocks = new ClaimBlocks();

        @Getter
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
    @ConfigSerializable
    public static class Dialogs {

        private Search search = new Search();

        @Setting("rename-claim")
        private SingleInput renameClaim = new SingleInput(
                "Rename Your Claim", "Input the new name",
                Map.of(
                        "submit", new Button(
                                "Confirm", "Click to rename your claim"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        @Setting("rename-role")
        private SingleInput renameRole = new SingleInput(
                "Rename | <role_name>", "Input the new name",
                Map.of(
                        "submit", new Button(
                                "Confirm", "Click to rename role"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        private SingleInput setting = new SingleInput(
                "Change Setting | <claim_name>", "<setting_name>",
                Map.of(
                        "submit", new Button(
                                "Confirm", "Click to change setting"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        @Setting("create-role")
        private SingleInput createRole = new SingleInput(
                "Create New Role", "Role Name",
                Map.of(
                        "submit", new Button(
                                "Confirm", "Click to create role"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        @Getter
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
        @ConfigSerializable
        public static class SingleInput {

            private String title;

            private String input;

            private Map<String, Button> buttons;

            public SingleInput(String title, String input, Map<String, Button> buttons) {
                this.title = title;
                this.input = input;
                this.buttons = buttons;
            }

            public SingleInput() {
            }
        }

        @Getter
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

        @Getter
        @ConfigSerializable
        public static class InputBased {

        }
    }

    @Getter
    @ConfigSerializable
    public static class Inputs {

        private String cancelled = "<red>Input cancelled.";

        private Title title = new Title();

        @Setting("action-bar")
        private ActionBar actionBar = new ActionBar();

        @Getter
        @ConfigSerializable
        public static class Title {

            private boolean enabled = true;

            private String title = "<yellow>Enter Input";

            private String subtitle = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }

        @Getter
        @ConfigSerializable
        public static class ActionBar {

            private boolean enabled = true;

            private String text = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }
    }
}