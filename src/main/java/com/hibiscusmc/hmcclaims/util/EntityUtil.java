package com.hibiscusmc.hmcclaims.util;

import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityUtil {

    /**
     * The player behind an entity: the player itself, or the player that shot a projectile.
     */
    public static @Nullable Player responsiblePlayer(@Nullable Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }

        if (entity instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
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
