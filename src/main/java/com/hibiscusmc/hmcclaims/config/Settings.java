package com.hibiscusmc.hmcclaims.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class Settings {

    @Comment("Manages how the data will be stored")
    private Storage storage = new Storage();

    @Getter
    @ConfigSerializable
    public static class Storage {

        @Comment("""
                Method used to store claims data.
                
                ┌─ Available Methods:
                │
                ├─ MariaDB (Recommended!) (Remote - Default Port: 3306)
                └─ H2 (Local - Flatfile)""")
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

        @Comment("The name of the database where the data will be stored")
        private String database = "hmcclaims";

        @Comment("The prefix that will be used for every table / collection.")
        private String prefix = "hmcclaims_";

        @Comment("Settings for the remote connection. Don't worry about this if you're using a local method!")
        private Remote remote = new Remote();

        @Getter
        @ConfigSerializable
        public static class Remote {

            @Comment("The URI/Connection String for the database. Setting this will override every other value!")
            private String uri = "";

            @Comment("The address where the database is hosted. Don't include the port here!")
            private String address = "localhost";

            @Comment("The port of your database")
            private int port = 3306;

            @Comment("The credentials that will be used for the connection")
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
        @Comment("The amount of claim blocks every player will begin with")
        private int startingAmount = 100;

        @Comment("How much every claim blocks will cost. You need Vault for this!")
        private int price = 1;

    }

    private Claiming claiming = new Claiming();

    @Getter
    @ConfigSerializable
    public static class Claiming {

        @Setting("claim-tool")
        @Comment("The tool required to select land")
        private ItemStack claimTool = ItemStack.of(Material.GOLDEN_SHOVEL);
    }

    @Setting("world-aliases")
    @Comment("Aliases for worlds to display in different areas of the plugin")
    private Map<String, String> worldAliases = Map.of(
            "world", "Overworld",
            "world_nether", "Nether",
            "world_the_end", "The End"
    );
}