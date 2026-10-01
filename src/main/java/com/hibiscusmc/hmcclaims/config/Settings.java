package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Comment;
import team.hypox.config.core.annotation.Config;
import team.hypox.config.core.annotation.Section;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Config
@SuppressWarnings({"FieldMayBeFinal"})
public class Settings {

    @Comment("Manages how the data will be stored")
    private Storage storage = new Storage();

    @Getter
    @Section
    public static class Storage {

        @Comment("""
                Method used to store claims data.
                
                ┌─ Available Methods:
                │
                ├─ MariaDB (Recommended!) (Remote - Default Port: 3306)
                └─ H2 (Default) (Local - Flatfile)""")
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
        @Section
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

    private ClaimBlocks claimBlocks = new ClaimBlocks();

    @Getter
    @Section
    public static class ClaimBlocks {

        @Comment("The amount of claim blocks every player will begin with")
        private int startingAmount = 100;

        @Comment("Lets players buy claim blocks with /claimblocks buy. You need Vault and an economy plugin for this!")
        private Purchase purchase = new Purchase();

        @Getter
        @Section
        public static class Purchase {

            @Comment("Whether players are able to buy claim blocks at all")
            private boolean enabled = true;

            @Comment("How much a single claim block costs")
            private double price = 1.0;

            @Comment("The least amount of claim blocks a player can buy at once")
            private int minAmount = 1;

            @Comment("The most claim blocks a player can buy at once. Set to -1 for no limit.")
            private int maxAmount = 10_000;
        }
    }

    @Comment("Defines the cooldown between sending missing permission notifications. Set to -1 to disable.")
    private long notificationCooldown = 1_000;

    private Claiming claiming = new Claiming();

    @Getter
    @Section
    public static class Claiming {

        @Comment("The tool required to select land")
        private ConfigItem claimTool = ConfigItem.of(Material.GOLDEN_SHOVEL);

        @Comment("If the item should be strictly the same as the set in the config")
        private boolean claimToolStrict = true;
    }

    @Comment("Aliases for worlds to display in different areas of the plugin")
    private Map<String, String> worldAliases = MapUtil.ordered(
            "world", "Overworld",
            "world_nether", "Nether",
            "world_the_end", "The End"
    );

    @Comment("List of worlds where players won't be able to create claims. Use % to match everything after or before.")
    private Set<String> disabledWorlds = new LinkedHashSet<>(List.of(
            "world_%_end",
            "testing_world"
    ));

    @Comment("If it should announce that this world is disabled or let them interact with the item")
    private boolean announceDisabledWorld = true;

    private Guis guis = new Guis();

    private Forms forms = new Forms();

    @Getter
    @Section
    public static class Forms {

        @Comment("Give Bedrock players (detected through Floodgate) native forms instead of the inventory GUIs.")
        private boolean enabled = true;

        @Comment("Use the Bedrock texture of the GUI icon's material for buttons without an image.")
        private boolean materialImageFallback = true;
    }

    @Getter
    @Section
    public static class Guis {

        @Comment("The gui opened by /claims. LIST shows the claim list, FIRST_CLAIM opens the first claim.")
        private ClaimsGui claimsGui = ClaimsGui.LIST;

        public enum ClaimsGui {
            LIST,
            FIRST_CLAIM
        }
    }
}