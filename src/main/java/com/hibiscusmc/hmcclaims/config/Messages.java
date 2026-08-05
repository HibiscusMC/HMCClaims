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
    private String pluginReload = "<prefix><gray>Plugin reloaded!";

    private Claims claims = new Claims();

    private Commands commands = new Commands();

    private Dialogs dialogs = new Dialogs();

    private Inputs inputs = new Inputs();

    @Getter
    @ConfigSerializable
    public static class Claims {

        private String disabled = "<prefix><red>Claims are disabled in this world!";

        @Setting("owned-by")
        private String ownedBy = "<prefix><gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        private String created = "<prefix><gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

        private String resized = "<prefix><gray>The claim <#d24c9f><name> <gray>has been resized! Price: <#d24c9f><price>";

        private String deleted = "<prefix><green>Claim <white><claim_name></white> deleted successfully!";

        @Setting("dont-have-any")
        private String dontHaveAny = "<prefix><red>You don't have any claims";

        @Setting("sub-claim-created")
        private String subCreated = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been created!";

        @Setting("sub-claim-resized")
        private String subResized = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been resized!";

        @Setting("member-added")
        private String memberAdded = "<prefix><green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!";

        @Setting("member-remove")
        private String memberRemoved = "<prefix><red>Player <white><player_head> <name></white> <red>removed from <white><claim></white>!";

        @Setting("member-already-added")
        private String memberAlreadyAdded = "<prefix><red>This player is already in the claim!";

        @Setting("member-already-banned")
        private String memberAlreadyBanned = "<prefix><red>This player is already banned!";

        @Setting("cant-ban-member")
        private String cantBanMember = "<prefix><red>You can't ban this member!";

        @Setting("player-not-member")
        private String playerNotMember = "<prefix><red>This player is not a member of this claim!";

        @Setting("self-already-owner")
        private String selfAlreadyOwner = "<prefix><red>You're already the owner of this claim!";

        @Setting("member-already-owner")
        private String memberAlreadyOwner = "<prefix><red>This member is already the owner of this claim!";

        @Setting("claim-transferred")
        private String claimTransferred = "<prefix><green>Claim <white><claim></white> transferred to <white><player_head> <name></white>!";

        @Setting("member-banned")
        private String memberBanned = "<prefix><green>Player <white><player_head> <name></white> was banned from the claim <white><claim></white>!";

        private Permissions permissions = new Permissions();

        private Settings settings = new Settings();

        private Selecting selecting = new Selecting();

        private Resizing resizing = new Resizing();

        @Getter
        @ConfigSerializable
        public static class Permissions {

            @Setting("block-break")
            private String breakBlock = "<prefix><red>You can't break blocks in this claim.";

            @Setting("block-place")
            private String placeBlock = "<prefix><red>You can't place blocks in this claim.";

            @Setting("block-interact")
            private String interactBlock = "<prefix><red>You can't interact with blocks in this claim.";

            @Setting("entity-interact")
            private String interactEntity = "<prefix><red>You can't interact with entities in this claim.";

            @Setting("entity-damage")
            private String damageEntity = "<prefix><red>You can't damage entities in this claim.";

            @Setting("item-use")
            private String useItem = "<prefix><red>You can't use items in this claim.";

            @Setting("item-pickup")
            private String pickupItem = "<prefix><red>You can't pick up items in this claim.";

            @Setting("item-drop")
            private String dropItem = "<prefix><red>You can't drop items in this claim.";

            @Setting("damage-player")
            private String damagePlayer = "<prefix><red>You can't attack players in this claim.";

            @Setting("ignite-block")
            private String igniteBlock = "<prefix><red>You can't ignite blocks in this claim.";

            @Setting("player-interact")
            private String playerInteract = "<prefix><red>You can't interact with that in this claim.";

            @Setting("use-redstone")
            private String useRedstone = "<prefix><red>You can't use redstone in this claim.";

            @Setting("use-door")
            private String useDoor = "<prefix><red>You can't use doors in this claim.";

            @Setting("use-trapdoor")
            private String useTrapdoor = "<prefix><red>You can't use trapdoors in this claim.";

            @Setting("allow-flight")
            private String allowFlight = "<prefix><red>You can't fly in this claim.";

            @Setting("use-elytra")
            private String useElytra = "<prefix><red>You can't use elytras in this claim.";

            @Setting("ignore-locked")
            private String ignoreLocked = "<prefix><red>You can't enter this locked claim.";

            @Setting("use-vehicle")
            private String useVehicle = "<prefix><red>You can't use vehicles in this claim.";

            @Setting("trample-soil")
            private String trampleSoil = "<prefix><red>You can't trample farmland in this claim.";

            @Setting("harvest-crops")
            private String harvestCrops = "<prefix><red>You can't harvest crops in this claim.";

            @Setting("plant-crops")
            private String plantCrops = "<prefix><red>You can't plant crops in this claim.";

            @Setting("use-wind-charge")
            private String useWindCharge = "<prefix><red>You can't use wind charges in this claim.";
        }

        @Getter
        @ConfigSerializable
        public static class Settings {

            @Setting("join-message")
            private String joinMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";

            @Setting("leave-message")
            private String leaveMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";
        }

        @Getter
        @ConfigSerializable
        public static class Selecting {

            @Setting("first-selection")
            private String firstSelection = "<prefix><gray>Selected <#d24c9f>first corner <gray>at location <#d24c9f><location><gray>.";

            @Setting("second-selection")
            private String secondSelection = "<prefix><gray>Selected <#d24c9f>second corner <gray>at location <#d24c9f><location><gray>. <#d24c9f>Left-Click <gray>to create your claim!";

            @Setting("selection-removed")
            private String selectionRemoved = "<prefix><gray>Your selection has been removed!";

            @Setting("corner-unselected")
            private String cornerUnselected = "<prefix><gray>Corner at location <#d24c9f><location> <gray>has been unselected.";

            @Setting("must-select-region")
            private String mustSelectRegion = "<prefix><red>You must select a region with <#d24c9f>Left-Click<red>!";

            @Setting("must-select-points")
            private String mustSelectPoints = "<prefix><red>You must select two points!";

            @Setting("claim-overlaps")
            private String claimOverlaps = "<prefix><red>There's already a claim on this area!";

            @Setting("selection-too-small")
            private String selectionTooSmall = "<prefix><red>The selected region is too small! <gray>(Should be at least 5x5)";

            @Setting("not-enough-claimblocks")
            private String notEnoughClaimBlocks = "<prefix><red>You don't have enough claim blocks! <gray>Required: <white><required_blocks></white>, <gray>Current: <white><current_blocks>";

            @Setting("land-already-claimed")
            private String landAlreadyClaimed = "<prefix><red>Someone already claimed this land!";

            @Setting("resizing-wrong-claim")
            private String resizingWrongClaim = "<prefix><red>You are not resizing this claim!";

            @Setting("claim-within-sub")
            private String claimWithinSub = "<prefix><red>Can't create a claim within a sub claim";

            @Setting("sub-outside-boundaries")
            private String subOutsideBoundaries = "<prefix><red>Sub claim can't be outside of main claim boundaries";
        }

        @Getter
        @ConfigSerializable
        public static class Resizing {

            @Setting("select-a-corner")
            private String selectACorner = "<prefix><red>You should select a corner";

            @Setting("enclose-claim-boundaries")
            private String encloseClaimBoundaries = "<prefix><red>The new region must completely enclose the original claim boundaries!";

            @Setting("within-main-claim")
            private String withinMainClaim = "<prefix><red>The new region must be inside of the main claim boundaries!";

            @Setting("sub-within-sub")
            private String subWithinSub = "<prefix><red>There's already a sub claim here!";
        }
    }

    @Getter
    @ConfigSerializable
    public static class Commands {

        @Setting("no-permission")
        private String noPermission = "<prefix><red>No permissions.";

        private String usage = "<prefix><gray>Command usage: <white>/<command> <usage>";

        @Setting("invalid-argument")
        private String invalidArgument = "<prefix><red>Invalid argument provided!";

        @Setting("missing-player")
        private String missingPlayer = "<prefix><red>You need to specify a player!";

        @Setting("player-not-found")
        private String playerNotFound = "<prefix><red>Player not found";

        @Setting("not-your-claim")
        private String notYourClaim = "<prefix><red>This is not your claim!";

        @Setting("not-in-claim")
        private String notInClaim = "<prefix><red>You're not standing in a claim!";

        @Setting("claim-blocks")
        private ClaimBlocks claimBlocks = new ClaimBlocks();

        @Getter
        @ConfigSerializable
        public static class ClaimBlocks {

            private String summary = """
                    <prefix><gray><white><player_name></white>'s claim blocks breakdown:
                    
                    <dark_gray><b>»</b> <gray>Starting Blocks: <white><starting_blocks>
                    <dark_gray><b>»</b> <gray>Obtained Blocks: <white><obtained_blocks>
                    <dark_gray><b>»</b> <gray>Total Blocks: <white><total_blocks>
                    
                    <dark_gray><b>»</b> <gray>Used Blocks: <white><used_blocks>
                    <dark_gray><b>»</b> <gray>Available Blocks: <white><available_blocks>""";

            @Setting("set-amount")
            private String setAmount = "<prefix><gray>You set <white><amount></white> claim blocks to <white><name></white>!";

            @Setting
            private String invalid = "<prefix><red>Invalid amount of claim blocks!";

            @Setting("add-amount")
            private String addAmount = "<prefix><gray>You added <white><amount></white> claim blocks to <white><name></white>!\n<gray>New amount: <white><new_amount>";

            @Setting("remove-amount")
            private String removeAmount = "<prefix><gray>You removed <white><amount></white> claim blocks from <white><name></white>!\n<gray>New amount: <white><new_amount>";
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

        private String cancelled = "<prefix><red>Input cancelled.";

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