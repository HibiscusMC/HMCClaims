package com.hibiscusmc.hmcclaims.config.form;

import lombok.Getter;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.List;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class DialogsFormConfig extends FormTemplate {

    private SingleInput renameClaim = new SingleInput(
            "Rename claim",
            List.of("<gray>Currently called <white><claim_name></white>."),
            new Input("<white>New name", "Claim name...", 40)
    );

    private SingleInput renameRole = new SingleInput(
            "Rename role",
            List.of("<gray>Currently called <white><role_name></white>."),
            new Input("<white>New name", "Role name...", 40)
    );

    private SingleInput createRole = new SingleInput(
            "Create role",
            List.of("<gray>The new role starts with the default role's permissions."),
            new Input("<white>Role name", "Role name...", 40)
    );

    @Comment("Used for settings that hold a value rather than an on/off state")
    private SingleInput setting = new SingleInput(
            "<setting_name>",
            List.of("<gray>Leave this empty to clear the value."),
            new Input("<white>New value", "<setting_value>", 100)
    );

    private SingleInput addMember = new SingleInput(
            "Add member",
            List.of("<gray>The player has to have joined the server before."),
            new Input("<white>Player name", "Player name...", 16)
    );

    private SingleInput banMember = new SingleInput(
            "Ban a player",
            List.of("<gray>Banned players can't enter or interact with the claim."),
            new Input("<white>Player name", "Player name...", 16)
    );

    private SingleInput transferOwnership = new SingleInput(
            "Transfer ownership",
            List.of(
                    "<gray>The new owner takes full control of <white><claim_name></white>.",
                    "",
                    "<red>You can't undo this yourself."
            ),
            new Input("<white>Player name", "Player name...", 16)
    );

    @Comment("Sent when the player submits a form without filling the field in")
    private String emptyInput = "<prefix><red>You need to type something.";

    @Comment("Sent when the typed name doesn't match a player who has joined before")
    private String playerNotFound = "<prefix><red>Player not found.";

    @Comment("Shown before saving permission changes that have a configured warning")
    private Confirm confirm = new Confirm();

    private Confirmation expandClaim = new Confirmation(
            "Expand <claim_name>",
            List.of(
                    "<gray>Expand <white><claim_name></white> by <white><amount></white> blocks to the <white><direction></white>?",
                    "",
                    "<gray>New size: <white><width>x<length>",
                    "<gray>Price: <white><price></white> claim blocks <dark_gray>(<remaining_blocks> left after)"
            ),
            "Expand",
            "Cancel"
    );

    @Getter
    @Section
    public static class Confirm {

        private String title = "Confirm changes";

        private String confirm = "Confirm";

        private String cancel = "Cancel";

        @Comment("Placed between the warnings of each changed permission")
        private String separator = "\n\n";
    }

    @Getter
    @Section
    public static class SingleInput {

        private String title = "Input";

        @Comment("Shown above the field")
        private List<String> content = List.of();

        private Input input = new Input();

        public SingleInput() {
        }

        public SingleInput(String title, List<String> content, Input input) {
            this.title = title;
            this.content = content;
            this.input = input;
        }
    }

    @Getter
    @Section
    public static class Confirmation {

        private String title = "Confirm";

        private List<String> content = List.of();

        private String confirm = "Confirm";

        private String cancel = "Cancel";

        public Confirmation() {
        }

        public Confirmation(String title, List<String> content, String confirm, String cancel) {
            this.title = title;
            this.content = content;
            this.confirm = confirm;
            this.cancel = cancel;
        }
    }
}
