package com.hibiscusmc.hmcclaims.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
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
}
