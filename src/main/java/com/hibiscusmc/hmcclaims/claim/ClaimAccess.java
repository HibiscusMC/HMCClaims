package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Optional;

/**
 * Resolves what a player may do in a claim, taking staff claim bypass into account.
 */
@Singleton
public class ClaimAccess {

    public final static String BYPASS_PERMISSION = "hmcclaims.commands.admin.bypass";

    @Inject
    private UserManager users;

    /**
     * Whether the player has bypass enabled and is still allowed to use it.
     */
    public boolean isBypassing(@NotNull Player player) {
        return users.getUser(player.getUniqueId()).map(User::bypassMode).orElse(false)
                && player.hasPermission(BYPASS_PERMISSION);
    }

    /**
     * Whether the player holds the permission in the claim, or is bypassing.
     */
    public boolean hasPermission(@NotNull Player player, @NotNull Claim claim, @NotNull Permission permission) {
        return isBypassing(player) || claim.hasPermission(player.getUniqueId(), permission);
    }

    /**
     * The membership the player acts with in menus; bypassing players act as the owner.
     */
    @NotNull
    public Optional<ClaimMember> actor(@NotNull Player player, @NotNull Claim claim) {
        if (isBypassing(player)) {
            return claim.getMember(claim.owner());
        }

        return claim.getMember(player.getUniqueId());
    }

    /**
     * Whether the player owns the claim, or is bypassing.
     */
    public boolean isOwner(@NotNull Player player, @NotNull Claim claim) {
        return claim.owner().equals(player.getUniqueId()) || isBypassing(player);
    }

    /**
     * Whether the player may open the claim's menus: owner, co-owner or bypassing.
     */
    public boolean canOpen(@NotNull Player player, @NotNull Claim claim) {
        return isOwner(player, claim)
                || claim.getMember(player.getUniqueId()).map(ClaimMember::isCoOwner).orElse(false);
    }
}
