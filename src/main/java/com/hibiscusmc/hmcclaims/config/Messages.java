package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ToString
@ConfigSerializable
public class Messages {

    private String prefix = "<b><gradient:#49fc4f:#3ffcb4:#49fc4f>HMCClaims</gradient> <dark_gray>|</dark_gray></b> ";

    @Setting("reload")
    private String pluginReload = "<gray>Plugin reloaded!";

    private Claims claims = new Claims();

    @Getter
    @ToString
    @ConfigSerializable
    public static class Claims {

        @Setting("owned-by")
        private String ownedBy = "<gray>Claim <#d24c9f><name> <gray>is owned by <#d24c9f><owner>";

        @Setting("created")
        private String created = "<gray>The claim <#d24c9f><name> <gray>has been created! Price: <#d24c9f><price>";

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

        }
    }
}