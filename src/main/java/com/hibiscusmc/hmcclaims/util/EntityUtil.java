package com.hibiscusmc.hmcclaims.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityUtil {

    private static final int MAX_DEPTH = 3;

    /**
     * The player behind an entity: the player itself, the player that shot a projectile or threw a
     * lingering cloud, the player that ignited primed TNT, or the online owner of a tamed pet.
     */
    public static @Nullable Player responsiblePlayer(@Nullable Entity entity) {
        return responsiblePlayer(entity, 0);
    }

    private static @Nullable Player responsiblePlayer(@Nullable Entity entity, int depth) {
        if (entity instanceof Player player) {
            return player;
        }

        if (depth >= MAX_DEPTH) {
            return null;
        }

        if (entity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                return player;
            }

            return null;
        }

        if (entity instanceof AreaEffectCloud cloud) {
            if (cloud.getSource() instanceof Player player) {
                return player;
            }

            return null;
        }

        if (entity instanceof TNTPrimed tnt) {
            return responsiblePlayer(tnt.getSource(), depth + 1);
        }

        if (entity instanceof Tameable tameable) {
            UUID owner = tameable.getOwnerUniqueId();

            return owner != null ? Bukkit.getPlayer(owner) : null;
        }

        return null;
    }

    /**
     * How far from its current position a wind charge's explosion can push entities: twice its
     * explosion radius, plus the distance it still travels before exploding when it hits something.
     */
    public static double explosionReach(@NotNull AbstractWindCharge windCharge) {
        double pushRange = windCharge instanceof BreezeWindCharge ? 6.0 : 2.4;

        return pushRange + windCharge.getVelocity().length();
    }
}
