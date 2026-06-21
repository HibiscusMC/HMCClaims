package com.hibiscusmc.hmcclaims.claim.setting;

import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;

/**
 * Represents individual settings that can be granted to a claim.
 */
public record Setting<T>(Key key, String displayName, String description, T defaultValue) {

    public Setting(String id, String displayName, String description, T defaultValue) {
        this(RegistryUtil.key(id), displayName, description, defaultValue);
    }

    public final static Setting<Boolean> MOB_EXPLOSIONS =
            new Setting<>("mob_explosions", "Mob Explosions", "If mob explosions will damage the claim", true);
    public final static Setting<Boolean> BLOCK_EXPLOSIONS =
            new Setting<>("block_explosions", "Block Explosions", "If block explosions will damage the claim", true);
}