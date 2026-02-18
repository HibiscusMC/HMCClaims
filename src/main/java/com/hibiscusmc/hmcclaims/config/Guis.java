package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ToString
@ConfigSerializable
public class Guis {

    @Getter
    @ToString
    @ConfigSerializable
    public static class ClaimList extends Guis {

        private String title = "Your claims";
        private int rows = 5;

    }

    @Getter
    @ToString
    @ConfigSerializable
    public static class ClaimInfo extends Guis {

        private String title = "<claim_name> Info";
        private int rows = 5;

    }

}