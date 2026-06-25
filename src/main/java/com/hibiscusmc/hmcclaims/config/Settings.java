package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class Settings {

    private Storage storage = new Storage();

    @Getter
    @ConfigSerializable
    public static class Storage {

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

        private String database = "hmcclaims";
        private String prefix = "hmcclaims_";
        private Remote remote = new Remote();

        @Getter
        @ConfigSerializable
        public static class Remote {

            private String uri = "";
            private String address = "localhost";
            private int port = 3306;
            private String username = "root";
            private String password = "youshallnotpass";

        }
    }

    @Setting("claim-blocks")
    private ClaimBlocks claimBlocks = new ClaimBlocks();

    @Getter
    @ConfigSerializable
    public static class ClaimBlocks {

        @Setting("starting-amount")
        private int startingAmount = 100;

        private int price = 1;

    }

    private Claiming claiming = new Claiming();

    @Getter
    @ConfigSerializable
    public static class Claiming {

        @Setting("claim-tool")
        private ItemStack claimTool = ItemStack.of(Material.GOLDEN_SHOVEL);

    }

    @Setting("world-aliases")
    private Map<String, String> worldAliases = Map.of(
            "world", "Overworld",
            "world_nether", "Nether",
            "world_the_end", "The End"
    );
}