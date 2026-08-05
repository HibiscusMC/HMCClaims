package com.hibiscusmc.hmcclaims.claim.setting;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;

import java.util.function.Function;

/**
 * Represents individual settings that can be granted to a claim.
 */
public record Setting<T>(Key key, String displayName, String description, Function<String, T> parser, T defaultValue) {

    public Setting(String id, String displayName, String description, Function<String, T> parser, T defaultValue) {
        this(RegistryUtil.key(id), displayName, description, parser, defaultValue);
    }

    //
    public final static Setting<String> JOIN_MESSAGE =
            new Setting<>("join_message", "Join Message", "Sends a message when someone enters in the claim.", (str) -> str, "Welcome, $PLAYER!");
    public final static Setting<String> LEAVE_MESSAGE =
            new Setting<>("leave_message", "Leave Message", "Sends a message when someone leaves the claim.", (str) -> str, "Bye, $PLAYER!");

    public final static Setting<Boolean> MOB_EXPLOSIONS =
            new Setting<>("mob_explosions", "Mob Explosions", "If mob explosions will damage the claim", Boolean::parseBoolean, true);
    public final static Setting<Boolean> BLOCK_EXPLOSIONS =
            new Setting<>("block_explosions", "Block Explosions", "If block explosions will damage the claim", Boolean::parseBoolean, true);
}