package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ToString
@ConfigSerializable
public class Settings {

    private Storage storage = new Storage();

    @Getter
    @ToString
    @ConfigSerializable
    public static class Storage {

        private String database = "hmcclaims";
        private String prefix = "hmcclaims_";

        private Remote remote = new Remote();

        @Getter
        @ToString
        @ConfigSerializable
        public static class Remote {

            private String uri = "";
            private String address = "localhost";
            private int port = 3306;
            private String username = "root";
            private String password = "youshallnotpass";

        }

        private StorageMethod method = StorageMethod.H2;

        public enum StorageMethod {
            H2("H2"),
            MARIADB("MariaDB");

            private final String name;

            StorageMethod(String name) {
                this.name = name;
            }

            public String methodName() {
                return name;
            }

            @Override
            public String toString() {
                return name;
            }
        }

    }

    private Claiming claiming = new Claiming();

    @Getter
    @ToString
    @ConfigSerializable
    public static class Claiming {

        @Setting("claim-tool")
        private Material claimTool = Material.GOLDEN_HOE;

    }

}