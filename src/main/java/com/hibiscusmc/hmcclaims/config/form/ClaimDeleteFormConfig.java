package com.hibiscusmc.hmcclaims.config.form;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class ClaimDeleteFormConfig extends FormTemplate {

    private Title title = new Title("Delete <claim_name>?", 20);

    @Comment("Shown for a main claim")
    private List<String> content = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Location: <white><world>, X: <x>, Z: <z>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
            "<gray>Members: <white><member_count>",
            "<gray>Sub Claims: <white><total_sub_claims>",
            "",
            "<red>This cannot be undone."
    );

    @Setting("sub-claim-content")
    @Comment("Shown for a sub claim")
    private List<String> subClaimContent = List.of(
            "<gray>UID: <white><short_id>",
            "<gray>Main claim: <white><main_claim>",
            "<gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)",
            "<gray>Members: <white><member_count>",
            "",
            "<red>This cannot be undone."
    );

    @Comment("The left button. Bedrock modal buttons can't show an icon.")
    private String confirm = "<red>Delete";

    @Comment("The right button, also used when the player dismisses the form")
    private String cancel = "Cancel";
}