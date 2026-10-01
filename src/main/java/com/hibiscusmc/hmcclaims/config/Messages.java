package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Key;
import team.hypox.config.core.annotation.Section;

import java.util.Map;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class Messages {

    private String prefix = "<b><gradient:#49fc4f:#3ffcb4:#49fc4f>HMCClaims</gradient> <dark_gray>|</dark_gray></b> ";

    @Key("reload")
    private String pluginReload = "<prefix><gray>Plugin reloaded!";

    private Claims claims = new Claims();

    private Commands commands = new Commands();

    private Dialogs dialogs = new Dialogs();

    private Inputs inputs = new Inputs();

    @Getter
    @Section
    public static class Claims {

        private String disabled = "<prefix><red>Claims are disabled in this world!";

        private String ownedBy = "<prefix><gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        private String created = "<prefix><gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

        private String resized = "<prefix><gray>The claim <#d24c9f><name> <gray>has been resized! Price: <#d24c9f><price>";

        private String deleted = "<prefix><green>Claim <white><claim_name></white> deleted successfully!";

        private String dontHaveAny = "<prefix><red>You don't have any claims";

        @Key("sub-claim-created")
        private String subCreated = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been created!";

        @Key("sub-claim-resized")
        private String subResized = "<prefix><gray>The sub claim <#d24c9f><name> <gray>has been resized!";

        private String memberAdded = "<prefix><green>Player <white><player_head> <name></white> <green>added to <white><claim></white>!";

        @Key("member-remove")
        private String memberRemoved = "<prefix><red>Player <white><player_head> <name></white> <red>removed from <white><claim></white>!";

        private String memberAlreadyAdded = "<prefix><red>This player is already in the claim!";

        private String memberAlreadyBanned = "<prefix><red>This player is already banned!";

        private String cantBanMember = "<prefix><red>You can't ban this member!";

        private String playerNotMember = "<prefix><red>This player is not a member of this claim!";

        private String cantRemoveOwner = "<prefix><red>The owner can't be removed from their own claim!";

        private String selfAlreadyOwner = "<prefix><red>You're already the owner of this claim!";

        private String memberAlreadyOwner = "<prefix><red>This member is already the owner of this claim!";

        private String claimTransferred = "<prefix><green>Claim <white><claim></white> transferred to <white><player_head> <name></white>!";

        private String memberBanned = "<prefix><green>Player <white><player_head> <name></white> was banned from the claim <white><claim></white>!";

        private Permissions permissions = new Permissions();

        private Settings settings = new Settings();

        private Selecting selecting = new Selecting();

        private Resizing resizing = new Resizing();

        @Getter
        @Section
        public static class Permissions {

            @Key("block-break")
            private String breakBlock = "<prefix><red>You can't break blocks in this claim.";

            @Key("block-place")
            private String placeBlock = "<prefix><red>You can't place blocks in this claim.";

            @Key("block-interact")
            private String interactBlock = "<prefix><red>You can't interact with blocks in this claim.";

            @Key("entity-interact")
            private String interactEntity = "<prefix><red>You can't interact with entities in this claim.";

            @Key("entity-damage")
            private String damageEntity = "<prefix><red>You can't damage entities in this claim.";

            @Key("item-use")
            private String useItem = "<prefix><red>You can't use items in this claim.";

            @Key("item-pickup")
            private String pickupItem = "<prefix><red>You can't pick up items in this claim.";

            @Key("item-drop")
            private String dropItem = "<prefix><red>You can't drop items in this claim.";

            private String igniteBlock = "<prefix><red>You can't ignite blocks in this claim.";

            private String playerInteract = "<prefix><red>You can't interact with that in this claim.";

            private String useRedstone = "<prefix><red>You can't use redstone in this claim.";

            private String useDoor = "<prefix><red>You can't use doors in this claim.";

            private String useTrapdoor = "<prefix><red>You can't use trapdoors in this claim.";

            private String useLectern = "<prefix><red>You can't use lecterns in this claim.";

            private String takeLecternBook = "<prefix><red>You can't take books from lecterns in this claim.";

            private String putLecternBook = "<prefix><red>You can't place books on lecterns in this claim.";

            private String allowFlight = "<prefix><red>You can't fly in this claim.";

            private String useElytra = "<prefix><red>You can't use elytras in this claim.";

            private String ignoreLocked = "<prefix><red>You can't enter this locked claim.";

            private String useVehicle = "<prefix><red>You can't use vehicles in this claim.";

            private String trampleSoil = "<prefix><red>You can't trample farmland in this claim.";

            private String harvestCrops = "<prefix><red>You can't harvest crops in this claim.";

            private String plantCrops = "<prefix><red>You can't plant crops in this claim.";

            private String useWindCharge = "<prefix><red>You can't use wind charges in this claim.";

            private String memberBanned = "<prefix><red>You're banned from this claim.";
        }

        @Getter
        @Section
        public static class Settings {

            private String joinMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";

            private String leaveMessage = "<prefix><gray>[Claim <claim_name>] <white><message>";

            private String pvpDisabled = "<prefix><red>PvP is disabled in this claim.";
        }

        @Getter
        @Section
        public static class Selecting {

            private String firstSelection = "<prefix><gray>Selected <#d24c9f>first corner <gray>at location <#d24c9f><location><gray>.";

            private String secondSelection = "<prefix><gray>Selected <#d24c9f>second corner <gray>at location <#d24c9f><location><gray>. <#d24c9f>Left-Click <gray>to create your claim!";

            private String selectionRemoved = "<prefix><gray>Your selection has been removed!";

            private String cornerUnselected = "<prefix><gray>Corner at location <#d24c9f><location> <gray>has been unselected.";

            private String mustSelectRegion = "<prefix><red>You must select a region with <#d24c9f>Left-Click<red>!";

            private String mustSelectPoints = "<prefix><red>You must select two points!";

            private String claimOverlaps = "<prefix><red>There's already a claim on this area!";

            private String selectionTooSmall = "<prefix><red>The selected region is too small! <gray>(Should be at least 5x5)";

            @Key("not-enough-claimblocks")
            private String notEnoughClaimBlocks = "<prefix><red>You don't have enough claim blocks! <gray>Required: <white><required_blocks></white>, <gray>Current: <white><current_blocks>";

            private String landAlreadyClaimed = "<prefix><red>Someone already claimed this land!";

            private String resizingWrongClaim = "<prefix><red>You are not resizing this claim!";

            private String claimWithinSub = "<prefix><red>Can't create a claim within a sub claim";

            private String subOutsideBoundaries = "<prefix><red>Sub claim can't be outside of main claim boundaries";
        }

        @Getter
        @Section
        public static class Resizing {

            private String selectACorner = "<prefix><red>You should select a corner";

            private String encloseClaimBoundaries = "<prefix><red>The new region must completely enclose the original claim boundaries!";

            private String withinMainClaim = "<prefix><red>The new region must be inside of the main claim boundaries!";

            private String subWithinSub = "<prefix><red>There's already a sub claim here!";

            private String grabClaimTool = "<prefix><gray>Hold your <#d24c9f>claim tool <gray>to start resizing <#d24c9f><name><gray>!";

            private String started = "<prefix><gray>Resize mode <green>enabled <gray>for <#d24c9f><name><gray>!";

            private String cancelled = "<prefix><gray>Resize mode <red>disabled<gray>.";

            private String notResizing = "<prefix><red>You're not resizing any claim!";

            private String tutorial = """
                    <prefix><#d24c9f><b>How to resize <name></b>
                    <dark_gray><b>»</b> <gray>Right-Click one of the <white>glowing corners</white> to grab it.
                    <dark_gray><b>»</b> <gray>Right-Click where you want that corner to end up to <white>expand</white> the claim.
                    <dark_gray><b>»</b> <gray>Left-Click any block to <green>save</green> the new size.
                    <dark_gray><b>»</b> <gray>Sneak or click <click:run_command:'/claim cancelresize'><hover:show_text:'<red>Click to leave resize mode'><red>[Cancel]</red></hover></click> <gray>to leave resize mode.""";

            private Title title = new Title();

            private ActionBar actionBar = new ActionBar();

            @Getter
            @Section
            public static class Title {

                private boolean enabled = true;

                private String title = "<#ff8000><b>RESIZE MODE";

                private String subtitle = "<white>Right-Click <gray>to resize <dark_gray>| <white>Left-Click <gray>to save <dark_gray>| <#ff0000>Sneak <red>to cancel";
            }

            @Getter
            @Section
            public static class ActionBar {

                private boolean enabled = false;

                private String text = "<white>Right-Click <gray>to resize <dark_gray>| <white>Left-Click <gray>to save <dark_gray>| <#ff0000>Sneak <red>to cancel";
            }
        }
    }

    @Getter
    @Section
    public static class Commands {

        private String noPermission = "<prefix><red>No permissions.";

        private String usage = "<prefix><gray>Command usage: <white>/<command> <usage>";

        private String invalidArgument = "<prefix><red>Invalid argument provided!";

        private String missingPlayer = "<prefix><red>You need to specify a player!";

        private String playerNotFound = "<prefix><red>Player not found";

        private String notYourClaim = "<prefix><red>This is not your claim!";

        private String notInClaim = "<prefix><red>You're not standing in a claim!";

        private ClaimBlocks claimBlocks = new ClaimBlocks();

        @Getter
        @Section
        public static class ClaimBlocks {

            private String summary = """
                    <prefix><gray><white><player_name></white>'s claim blocks breakdown:
                    
                    <dark_gray><b>»</b> <gray>Starting Blocks: <white><starting_blocks>
                    <dark_gray><b>»</b> <gray>Obtained Blocks: <white><obtained_blocks>
                    <dark_gray><b>»</b> <gray>Total Blocks: <white><total_blocks>
                    
                    <dark_gray><b>»</b> <gray>Used Blocks: <white><used_blocks>
                    <dark_gray><b>»</b> <gray>Available Blocks: <white><available_blocks>""";

            private String setAmount = "<prefix><gray>You set <white><amount></white> claim blocks to <white><name></white>!";

            private String invalid = "<prefix><red>Invalid amount of claim blocks!";

            private String addAmount = "<prefix><gray>You added <white><amount></white> claim blocks to <white><name></white>!\n<gray>New amount: <white><new_amount>";

            private String removeAmount = "<prefix><gray>You removed <white><amount></white> claim blocks from <white><name></white>!\n<gray>New amount: <white><new_amount>";

            private String purchased = "<prefix><gray>You bought <white><amount></white> claim blocks for <white><cost></white>!\n<gray>New amount: <white><new_amount>";

            private String purchaseDisabled = "<prefix><red>Claim blocks can't be bought on this server.";

            private String purchaseOutOfRange = "<prefix><red>You can only buy between <white><min></white> and <white><max></white> claim blocks at once!";

            private String notEnoughMoney = "<prefix><red>You don't have enough money! <gray>Cost: <white><cost></white>, <gray>Balance: <white><balance>";

            private String purchaseFailed = "<prefix><red>The purchase couldn't be completed. Please try again later.";
        }
    }

    @Getter
    @Section
    public static class Dialogs {

        private Search search = new Search();

        private SingleInput renameClaim = new SingleInput(
                "Rename Your Claim", "Input the new name",
                MapUtil.ordered(
                        "submit", new Button(
                                "Confirm", "Click to rename your claim"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        private SingleInput renameRole = new SingleInput(
                "Rename | <role_name>", "Input the new name",
                MapUtil.ordered(
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
                MapUtil.ordered(
                        "submit", new Button(
                                "Confirm", "Click to change setting"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        private SingleInput createRole = new SingleInput(
                "Create New Role", "Role Name",
                MapUtil.ordered(
                        "submit", new Button(
                                "Confirm", "Click to create role"
                        ),
                        "cancel", new Button(
                                "Cancel", "Click to cancel"
                        )
                )
        );

        @Getter
        @Section
        public static class Search {

            private String title = "Search";

            private String query = "Query";

            private String optionTitle = "Search by";

            private Map<String, String> options = MapUtil.ordered(
                    "name", "Claim Name",
                    "id", "Claim Id",
                    "main", "Main Claim Name",
                    "member", "Member Name"
            );

            private Map<String, Button> buttons = MapUtil.ordered(
                    "submit", new Button(
                            "Search", "Click to search"
                    ),
                    "cancel", new Button(
                            "Cancel", "Click to cancel"
                    )
            );
        }

        @Getter
        @Section
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
        @Section
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
        @Section
        public static class InputBased {

        }
    }

    @Getter
    @Section
    public static class Inputs {

        private String cancelled = "<prefix><red>Input cancelled.";

        private Title title = new Title();

        private ActionBar actionBar = new ActionBar();

        @Getter
        @Section
        public static class Title {

            private boolean enabled = true;

            private String title = "<yellow>Enter Input";

            private String subtitle = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }

        @Getter
        @Section
        public static class ActionBar {

            private boolean enabled = true;

            private String text = "<green>Type in chat <gray>• <#ff0000>Sneak <red>to cancel";
        }
    }
}