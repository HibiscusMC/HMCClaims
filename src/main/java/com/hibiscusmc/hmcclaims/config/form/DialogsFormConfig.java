package com.hibiscusmc.hmcclaims.config.form;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class DialogsFormConfig extends FormTemplate {

    @Setting("rename-claim")
    private SingleInput renameClaim = new SingleInput(
            "Rename claim",
            List.of("<gray>Currently called <white><claim_name></white>."),
            new Input("<white>New name", "Claim name...", 40)
    );

    @Setting("rename-role")
    private SingleInput renameRole = new SingleInput(
            "Rename role",
            List.of("<gray>Currently called <white><role_name></white>."),
            new Input("<white>New name", "Role name...", 40)
    );

    @Setting("create-role")
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

    @Setting("add-member")
    private SingleInput addMember = new SingleInput(
            "Add member",
            List.of("<gray>The player has to have joined the server before."),
            new Input("<white>Player name", "Player name...", 16)
    );

    @Setting("ban-member")
    private SingleInput banMember = new SingleInput(
            "Ban a player",
            List.of("<gray>Banned players can't enter or interact with the claim."),
            new Input("<white>Player name", "Player name...", 16)
    );

    @Setting("transfer-ownership")
    private SingleInput transferOwnership = new SingleInput(
            "Transfer ownership",
            List.of(
                    "<gray>The new owner takes full control of <white><claim_name></white>.",
                    "",
                    "<red>You can't undo this yourself."
            ),
            new Input("<white>Player name", "Player name...", 16)
    );

    @Setting("empty-input")
    @Comment("Sent when the player submits a form without filling the field in")
    private String emptyInput = "<prefix><red>You need to type something.";

    @Setting("player-not-found")
    @Comment("Sent when the typed name doesn't match a player who has joined before")
    private String playerNotFound = "<prefix><red>Player not found.";

    @Getter
    @ConfigSerializable
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
}