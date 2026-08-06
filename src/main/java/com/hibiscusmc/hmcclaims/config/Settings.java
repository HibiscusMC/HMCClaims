package com.hibiscusmc.hmcclaims.config;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;
import java.util.Set;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class Settings {

    public final static Object2BooleanMap<String> INVALID_WORLDS
            = new Object2BooleanOpenHashMap<>();

    @Comment("Manages how the data will be stored")
    private Storage storage = new Storage();

    @Getter
    @ConfigSerializable
    public static class Storage {

        @Setting(required = true)
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

        @Setting(required = true)
        @Comment("The name of the database where the data will be stored")
        private String database = "hmcclaims";

        @Setting(required = true)
        @Comment("The prefix that will be used for every table / collection.")
        private String prefix = "hmcclaims_";

        @Comment("Settings for the remote connection. Don't worry about this if you're using a local method!")
        private Remote remote = new Remote();

        @Getter
        @ConfigSerializable
        public static class Remote {

            @Setting(required = true)
            @Comment("The URI/Connection String for the database. Setting this will override every other value!")
            private String uri = "";

            @Setting(required = true)
            @Comment("The address where the database is hosted. Don't include the port here!")
            private String address = "localhost";

            @Setting(required = true)
            @Comment("The port of your database")
            private int port = 3306;

            @Setting(required = true)
            @Comment("The credentials that will be used for the connection")
            private String username = "root";
            @Setting(required = true)
            private String password = "youshallnotpass";
        }
    }

    @Setting("claim-blocks")
    private ClaimBlocks claimBlocks = new ClaimBlocks();

    @Getter
    @ConfigSerializable
    public static class ClaimBlocks {

        @Setting(value = "starting-amount", required = true)
        @Comment("The amount of claim blocks every player will begin with")
        private int startingAmount = 100;

        @Setting(required = true)
        @Comment("How much every claim blocks will cost. You need Vault for this!")
        private int price = 1;
    }

    @Setting(value = "notification-cooldown", required = true)
    @Comment("Defines the cooldown between sending missing permission notifications. Set to -1 to disable.")
    private long notificationCooldown = 1_000;

    private Claiming claiming = new Claiming();

    @Getter
    @ConfigSerializable
    public static class Claiming {

        @Setting(value = "claim-tool", required = true)
        @Comment("The tool required to select land")
        private ItemStack claimTool = ItemStack.of(Material.GOLDEN_SHOVEL);

        @Setting(value = "claim-tool-strict", required = true)
        @Comment("If the item should be strictly the same as the set in the config")
        private boolean claimToolStrict = true;
    }

    @Setting(value = "world-aliases", required = true)
    @Comment("Aliases for worlds to display in different areas of the plugin")
    private Map<String, String> worldAliases = Map.of(
            "world", "Overworld",
            "world_nether", "Nether",
            "world_the_end", "The End"
    );

    @Setting(value = "disabled-worlds", required = true)
    @Comment("List of worlds where players won't be able to create claims. Use % to match everything after or before.")
    private Set<String> disabledWorlds = Set.of(
            "world_%_end",
            "testing_world"
    );

    @Setting(value = "announce-disabled-world", required = true)
    @Comment("If it should announce that this world is disabled or let them interact with the item")
    private boolean announceDisabledWorld = true;

    private Guis guis = new Guis();

    @Getter
    @ConfigSerializable
    public static class Guis {

        @Setting(value = "claims-gui", required = true)
        @Comment("""
                The gui that will be opened when running /claims
                
                ┌─ Options:
                ├─ LIST
                ├   Uses the claim-list.yml config file. It will only open the list of
                │   claims this player has access to
                ├─ FIRST_CLAIM
                ├   Uses the claim-members.yml config file. It will open the first claim
                └   that appears on the player's list of claims""")
        private ClaimsGui claimsGui = ClaimsGui.LIST;

        public enum ClaimsGui {
            LIST,
            FIRST_CLAIM
        }
    }
}