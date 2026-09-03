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

    @Setting(value = "reload", required = true)
    private String pluginReload = "<prefix><gray>Plugin reloaded!";

    private Claims claims = new Claims();

    private Commands commands = new Commands();

    private Dialogs dialogs = new Dialogs();

    private Inputs inputs = new Inputs();

    @Getter
    @ConfigSerializable
    public static class Claims {

        @Setting(required = true)
        private String disabled = "<prefix><red>Claims are disabled in this world!";

        @Setting(value = "owned-by", required = true)
        private String ownedBy = "<prefix><gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        @Setting(required = true)
        private String created = "<prefix><gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

        @Setting(required = true)
        private String resized = "<prefix><gray>The claim <#d24c9f><name> <gray>has been resized! Price: <#d24c9f><price>";

        @Setting(required = true)
        private String deleted = "<prefix><green>Claim <white><claim_name></white> deleted successfully!";

        @Setting(value = "dont-have-any", required = true)
        private String dontHaveAny = "<prefix><red>You don't have any claims";

        @Setting(value = "sub-claim-created", required = true)
        private String subCreated = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been created!";

        @Setting(value = "sub-claim-resized", required = true)
        private String subResized = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been resized!";

        @Setting(value = "member-added", required = true)
        private String memberAdded = "<prefix><green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!";

        @Setting(value = "member-remove", required = true)
        private String memberRemoved = "<prefix><red>Player <white><player_head> <name></white> <red>removed from <white><claim></white>!";

        @Setting(value = "member-already-added", required = true)
        private String memberAlreadyAdded = "<prefix><red>This player is already in the claim!";

        @Setting(value = "member-already-banned", required = true)
        private String memberAlreadyBanned = "<prefix><red>This player is already banned!";

        @Setting(value = "cant-ban-member", required = true)
        private String cantBanMember = "<prefix><red>You can't ban this member!";

        @Setting(value = "player-not-member", required = true)
        private String playerNotMember = "<prefix><red>This player is not a member of this claim!";

        @Setting(value = "self-already-owner", required = true)
        private String selfAlreadyOwner = "<prefix><red>You're already the owner of this claim!";

        @Setting(value = "member-already-owner", required = true)
        private String memberAlreadyOwner = "<prefix><red>This member is already the owner of this claim!";

        @Setting(value = "claim-transferred", required = true)
        private String claimTransferred = "<prefix><green>Claim <white><claim></white> transferred to <white><player_head> <name></white>!";

        @Setting(value = "member-banned", required = true)
        private String memberBanned = "<prefix><green>Player <white><player_head> <name></white> was banned from the claim <white><claim></white>!";

        private Permissions permissions = new Permissions();

        private Settings settings = new Settings();

        private Selecting selecting = new Selecting();

        private Resizing resizing = new Resizing();

        @Getter
        @ConfigSerializable
        public static class Permissions {

            @Setting(value = "block-break", required = true)
            private String breakBlock = "<prefix><red>You can't break blocks in this claim.";

            @Setting(value = "block-place", required = true)
            private String placeBlock = "<prefix><red>You can't place blocks in this claim.";

            @Setting(value = "block-interact", required = true)
            private String interactBlock = "<prefix><red>You can't interact with blocks in this claim.";

            @Setting(value = "entity-interact", required = true)
            private String interactEntity = "<prefix><red>You can't interact with entities in this claim.";

            @Setting(value = "entity-damage", required = true)
            private String damageEntity = "<prefix><red>You can't damage entities in this claim.";

            @Setting(value = "item-use", required = true)
            private String useItem = "<prefix><red>You can't use items in this claim.";

            @Setting(value = "item-pickup", required = true)
            private String pickupItem = "<prefix><red>You can't pick up items in this claim.";

            @Setting(value = "item-drop", required = true)
            private String dropItem = "<prefix><red>You can't drop items in this claim.";

            @Setting(value = "ignite-block", required = true)
            private String igniteBlock = "<prefix><red>You can't ignite blocks in this claim.";

            @Setting(value = "player-interact", required = true)
            private String playerInteract = "<prefix><red>You can't interact with that in this claim.";

            @Setting(value = "use-redstone", required = true)
            private String useRedstone = "<prefix><red>You can't use redstone in this claim.";

            @Setting(value = "use-door", required = true)
            private String useDoor = "<prefix><red>You can't use doors in this claim.";

            @Setting(value = "use-trapdoor", required = true)
            private String useTrapdoor = "<prefix><red>You can't use trapdoors in this claim.";

            @Setting(value = "use-lectern", required = true)
            private String useLectern = "<prefix><red>You can't use lecterns in this claim.";

            @Setting(value = "take-lectern-book", required = true)
            private String takeLecternBook = "<prefix><red>You can't take books from lecterns in this claim.";

            @Setting(value = "put-lectern-book", required = true)
            private String putLecternBook = "<prefix><red>You can't place books on lecterns in this claim.";

            @Setting(value = "allow-flight", required = true)
            private String allowFlight = "<prefix><red>You can't fly in this claim.";

            @Setting(value = "use-elytra", required = true)
            private String useElytra = "<prefix><red>You can't use elytras in this claim.";

            @Setting(value = "ignore-locked", required = true)
            private String ignoreLocked = "<prefix><red>You can't enter this locked claim.";

            @Setting(value = "use-vehicle", required = true)
            private String useVehicle = "<prefix><red>You can't use vehicles in this claim.";

            @Setting(value = "trample-soil", required = true)
            private String trampleSoil = "<prefix><red>You can't trample farmland in this claim.";

            @Setting(value = "harvest-crops", required = true)
            private String harvestCrops = "<prefix><red>You can't harvest crops in this claim.";

            @Setting(value = "plant-crops", required = true)
            private String plantCrops = "<prefix><red>You can't plant crops in this claim.";

            @Setting(value = "use-wind-charge", required = true)
            private String useWindCharge = "<prefix><red>You can't use wind charges in this claim.";

            @Setting(value = "member-banned", required = true)
            private String memberBanned = "<prefix><red>You're banned from this claim.";
        }

        @Getter
        @ConfigSerializable
        public static class Settings {

            @Setting(value = "join-message", required = true)
            private String joinMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";

            @Setting(value = "leave-message", required = true)
            private String leaveMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";

            @Setting(value = "pvp-disabled", required = true)
            private String pvpDisabled = "<prefix><red>PvP is disabled in this claim.";
        }

        @Getter
        @ConfigSerializable
        public static class Selecting {

            @Setting(value = "first-selection", required = true)
            private String firstSelection = "<prefix><gray>Selected <#d24c9f>first corner <gray>at location <#d24c9f><location><gray>.";

            @Setting(value = "second-selection", required = true)
            private String secondSelection = "<prefix><gray>Selected <#d24c9f>second corner <gray>at location <#d24c9f><location><gray>. <#d24c9f>Left-Click <gray>to create your claim!";

            @Setting(value = "selection-removed", required = true)
            private String selectionRemoved = "<prefix><gray>Your selection has been removed!";

            @Setting(value = "corner-unselected", required = true)
            private String cornerUnselected = "<prefix><gray>Corner at location <#d24c9f><location> <gray>has been unselected.";

            @Setting(value = "must-select-region", required = true)
            private String mustSelectRegion = "<prefix><red>You must select a region with <#d24c9f>Left-Click<red>!";

            @Setting(value = "must-select-points", required = true)
            private String mustSelectPoints = "<prefix><red>You must select two points!";

            @Setting(value = "claim-overlaps", required = true)
            private String claimOverlaps = "<prefix><red>There's already a claim on this area!";

            @Setting(value = "selection-too-small", required = true)
            private String selectionTooSmall = "<prefix><red>The selected region is too small! <gray>(Should be at least 5x5)";

            @Setting(value = "not-enough-claimblocks", required = true)
            private String notEnoughClaimBlocks = "<prefix><red>You don't have enough claim blocks! <gray>Required: <white><required_blocks></white>, <gray>Current: <white><current_blocks>";

            @Setting(value = "land-already-claimed", required = true)
            private String landAlreadyClaimed = "<prefix><red>Someone already claimed this land!";

            @Setting(value = "resizing-wrong-claim", required = true)
            private String resizingWrongClaim = "<prefix><red>You are not resizing this claim!";

            @Setting(value = "claim-within-sub", required = true)
            private String claimWithinSub = "<prefix><red>Can't create a claim within a sub claim";

            @Setting(value = "sub-outside-boundaries", required = true)
            private String subOutsideBoundaries = "<prefix><red>Sub claim can't be outside of main claim boundaries";
        }

        @Getter
        @ConfigSerializable
        public static class Resizing {

            @Setting(value = "select-a-corner", required = true)
            private String selectACorner = "<prefix><red>You should select a corner";

            @Setting(value = "enclose-claim-boundaries", required = true)
            private String encloseClaimBoundaries = "<prefix><red>The new region must completely enclose the original claim boundaries!";

            @Setting(value = "within-main-claim", required = true)
            private String withinMainClaim = "<prefix><red>The new region must be inside of the main claim boundaries!";

            @Setting(value = "sub-within-sub", required = true)
            private String subWithinSub = "<prefix><red>There's already a sub claim here!";

            @Setting(value = "grab-claim-tool", required = true)
            private String grabClaimTool = "<prefix><gray>Hold your <#d24c9f>claim tool <gray>to start resizing <#d24c9f><name><gray>!";

            @Setting(required = true)
            private String started = "<prefix><gray>Resize mode <green>enabled <gray>for <#d24c9f><name><gray>!";

            @Setting(required = true)
            private String cancelled = "<prefix><gray>Resize mode <red>disabled<gray>.";

            @Setting(value = "not-resizing", required = true)
            private String notResizing = "<prefix><red>You're not resizing any claim!";

            @Setting(required = true)
            private String tutorial = """
                    <prefix><#d24c9f><b>How to resize <name></b>
                    <dark_gray><b>»</b> <gray>Right-Click one of the <white>glowing corners</white> to grab it.
                    <dark_gray><b>»</b> <gray>Right-Click where you want that corner to end up to <white>expand</white> the claim.
                    <dark_gray><b>»</b> <gray>Left-Click any block to <green>save</green> the new size.
                    <dark_gray><b>»</b> <gray>Sneak or click <click:run_command:'/claim cancelresize'><hover:show_text:'<red>Click to leave resize mode'><red>[Cancel]</red></hover></click> <gray>to leave resize mode.""";

            private Title title = new Title();

            @Setting("action-bar")
            private ActionBar actionBar = new ActionBar();

            @Getter
            @ConfigSerializable
            public static class Title {

                @Setting(required = true)
                private boolean enabled = true;

                @Setting(required = true)
                private String title = "<#ff8000><b>RESIZE MODE";

                @Setting(required = true)
                private String subtitle = "<white>Right-Click <gray>to resize <dark_gray>| <white>Left-Click <gray>to save <dark_gray>| <#ff0000>Sneak <red>to cancel";
            }

            @Getter
            @ConfigSerializable
            public static class ActionBar {

                @Setting(required = true)
                private boolean enabled = false;

                @Setting(required = true)
                private String text = "<white>Right-Click <gray>to resize <dark_gray>| <white>Left-Click <gray>to save <dark_gray>| <#ff0000>Sneak <red>to cancel";
            }
        }
    }

    @Getter
    @ConfigSerializable
    public static class Commands {

        @Setting(value = "no-permission", required = true)
        private String noPermission = "<prefix><red>No permissions.";

        @Setting(required = true)
        private String usage = "<prefix><gray>Command usage: <white>/<command> <usage>";

        @Setting(value = "invalid-argument", required = true)
        private String invalidArgument = "<prefix><red>Invalid argument provided!";

        @Setting(value = "missing-player", required = true)
        private String missingPlayer = "<prefix><red>You need to specify a player!";

        @Setting(value = "player-not-found", required = true)
        private String playerNotFound = "<prefix><red>Player not found";

        @Setting(value = "not-your-claim", required = true)
        private String notYourClaim = "<prefix><red>This is not your claim!";

        @Setting(value = "not-in-claim", required = true)
        private String notInClaim = "<prefix><red>You're not standing in a claim!";

        @Setting(value = "claim-blocks", required = true)
        private ClaimBlocks claimBlocks = new ClaimBlocks();

        @Getter
        @ConfigSerializable
        public static class ClaimBlocks {

            @Setting(required = true)
            private String summary = """
                    <prefix><gray><white><player_name></white>'s claim blocks breakdown:
                    
                    <dark_gray><b>»</b> <gray>Starting Blocks: <white><starting_blocks>
                    <dark_gray><b>»</b> <gray>Obtained Blocks: <white><obtained_blocks>
                    <dark_gray><b>»</b> <gray>Total Blocks: <white><total_blocks>
                    
                    <dark_gray><b>»</b> <gray>Used Blocks: <white><used_blocks>
                    <dark_gray><b>»</b> <gray>Available Blocks: <white><available_blocks>""";

            @Setting(value = "set-amount", required = true)
            private String setAmount = "<prefix><gray>You set <white><amount></white> claim blocks to <white><name></white>!";

            @Setting(required = true)
            private String invalid = "<prefix><red>Invalid amount of claim blocks!";

            @Setting(value = "add-amount", required = true)
            private String addAmount = "<prefix><gray>You added <white><amount></white> claim blocks to <white><name></white>!\n<gray>New amount: <white><new_amount>";

            @Setting(value = "remove-amount", required = true)
            private String removeAmount = "<prefix><gray>You removed <white><amount></white> claim blocks from <white><name></white>!\n<gray>New amount: <white><new_amount>";
        }
    }

    @Getter
    @ConfigSerializable
    public static class Dialogs {

        @Setting(required = true)
        private Search search = new Search();

        @Setting(value = "rename-claim", required = true)
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

        @Setting(value = "rename-role", required = true)
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

        @Setting(required = true)
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

        @Setting(value = "create-role", required = true)
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

            @Setting(required = true)
            private String title = "Search";

            @Setting(required = true)
            private String query = "Query";

            @Setting(value = "option-title", required = true)
            private String optionTitle = "Search by";

            @Setting(required = true)
            private Map<String, String> options = Map.of(
                    "name", "Claim Name",
                    "id", "Claim Id",
                    "main", "Main Claim Name",
                    "member", "Member Name"
            );

            @Setting(required = true)
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

            @Setting(required = true)
            private String title;

            @Setting(required = true)
            private String input;

            @Setting(required = true)
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

            @Setting(required = true)
            private String label = "Button Label";

            @Setting(required = true)
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

        @Setting(required = true)
        private String cancelled = "<prefix><red>Input cancelled.";

        private Title title = new Title();

        @Setting("action-bar")
        private ActionBar actionBar = new ActionBar();

        @Getter
        @ConfigSerializable
        public static class Title {

            @Setting(required = true)
            private boolean enabled = true;

            @Setting(required = true)
            private String title = "<yellow>Enter Input";

            @Setting(required = true)
            private String subtitle = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }

        @Getter
        @ConfigSerializable
        public static class ActionBar {

            @Setting(required = true)
            private boolean enabled = true;

            @Setting(required = true)
            private String text = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }
    }
}