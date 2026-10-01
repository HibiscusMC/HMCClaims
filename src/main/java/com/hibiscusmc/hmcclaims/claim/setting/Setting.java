package com.hibiscusmc.hmcclaims.claim.setting;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;

import java.util.function.Function;

/**
 * Represents individual settings that can be granted to a claim.
 */
public record Setting<T>(Key key, String displayName, String description, Function<String, T> parser, T defaultValue,
                         Material icon) {

    public Setting(Key key, String displayName, String description, Function<String, T> parser, T defaultValue) {
        this(key, displayName, description, parser, defaultValue, Material.BOOK);
    }

    public Setting(String id, String displayName, String description, Function<String, T> parser, T defaultValue) {
        this(RegistryUtil.key(id), displayName, description, parser, defaultValue, Material.BOOK);
    }

    public Setting(String id, String displayName, String description, Function<String, T> parser, T defaultValue, Material icon) {
        this(RegistryUtil.key(id), displayName, description, parser, defaultValue, icon);
    }

    //
    public final static Setting<String> JOIN_MESSAGE =
            new Setting<>("join_message", "Join Message", "Sends a message when someone enters in the claim.", (str) -> str, "Welcome, $PLAYER!", Material.OAK_SIGN);
    public final static Setting<String> LEAVE_MESSAGE =
            new Setting<>("leave_message", "Leave Message", "Sends a message when someone leaves the claim.", (str) -> str, "Bye, $PLAYER!", Material.BIRCH_SIGN);

    public final static Setting<Boolean> MOB_EXPLOSIONS =
            new Setting<>("mob_explosions", "Mob Explosions", "If mob explosions will damage the claim", Boolean::parseBoolean, true, Material.CREEPER_HEAD);
    public final static Setting<Boolean> BLOCK_EXPLOSIONS =
            new Setting<>("block_explosions", "Block Explosions", "If block explosions will damage the claim", Boolean::parseBoolean, true, Material.TNT);
    public final static Setting<Boolean> ENTITY_TRAMPLING =
            new Setting<>("entity_trampling", "Entity Trampling", "If mobs and animals can trample farmland in the claim.", Boolean::parseBoolean, false, Material.LEATHER_BOOTS);

    public final static Setting<Boolean> PVP =
            new Setting<>("pvp", "PvP", "If players can attack each other within the claim.", Boolean::parseBoolean, false, Material.DIAMOND_SWORD);
}