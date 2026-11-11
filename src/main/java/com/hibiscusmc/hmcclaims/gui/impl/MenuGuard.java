package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Re-evaluates what a player may do in a menu at the moment they click or submit, since menus,
 * dialogs and prompts can stay open long after they were rendered.
 */
@Singleton
public class MenuGuard {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ClaimAccess access;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ConfigHolder<Settings> settingsHolder;

    @Inject
    private TextUtil text;

    /**
     * Whether the claim is still loaded and is the very same instance the menu was opened with.
     */
    public boolean isActive(@NotNull Claim claim) {
        return claimManager.getClaim(claim.claimId()).orElse(null) == claim;
    }

    /**
     * The membership the player acts with, empty if the claim is no longer loaded.
     */
    @NotNull
    public Optional<ClaimMember> actor(@NotNull Player player, @NotNull Claim claim) {
        if (!isActive(claim)) {
            return Optional.empty();
        }

        return access.actor(player, claim);
    }

    public boolean isOwner(@NotNull Player player, @NotNull Claim claim) {
        return isActive(claim) && access.isOwner(player, claim);
    }

    public boolean canOpen(@NotNull Player player, @NotNull Claim claim) {
        return isActive(claim) && access.canOpen(player, claim);
    }

    public boolean hasPermission(@NotNull Player player, @NotNull Claim claim, @NotNull Permission permission) {
        return actor(player, claim)
                .map(member -> member.hasPermission(permission))
                .orElse(false);
    }

    /**
     * Whether the player can still manage the member: the claim is loaded, the member is still in it
     * and the player outranks them.
     */
    public boolean canManage(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimMember target) {
        if (claim.getMember(target.uuid()).orElse(null) != target) {
            return false;
        }

        return actor(player, claim)
                .map(member -> member.canManage(target))
                .orElse(false);
    }

    /**
     * Whether the role is still in the claim's registry.
     */
    public boolean roleExists(@NotNull Claim claim, @NotNull ClaimRole role) {
        return claim.roleRegistry().find(role.id()).orElse(null) == role;
    }

    public boolean canEditRolePermissions(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimRole role) {
        if (!roleExists(claim, role)) {
            return false;
        }

        return actor(player, claim)
                .map(member -> member.canEditRolePermissions(role))
                .orElse(false);
    }

    /**
     * Whether the player can still move the member into the role: they manage the member, hold
     * {@code MANAGE_MEMBER_ROLES} and the role ranks below their own, excluding owner and everyone.
     */
    public boolean canAssignRole(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimMember target, @NotNull ClaimRole role) {
        ClaimMember executor = actor(player, claim)
                .orElse(null);
        if (executor == null || !roleExists(claim, role)) {
            return false;
        }

        if (!executor.hasPermission(Permission.MANAGE_MEMBER_ROLES) || !canManage(player, claim, target)) {
            return false;
        }

        ClaimRoleRegistry registry = claim.roleRegistry();
        if (role.equals(registry.everyoneRole()) || role.equals(registry.ownerRole())) {
            return false;
        }

        List<ClaimRole> allRoles = registry.allRoles();

        return allRoles.indexOf(executor.role()) < allRoles.indexOf(role);
    }

    public boolean canGrant(@NotNull Player player, @NotNull Claim claim, @NotNull Permission permission) {
        return actor(player, claim)
                .map(member -> member.canGrant(permission))
                .orElse(false);
    }

    /**
     * The custom roles of the claim, not counting the owner, default and everyone roles.
     */
    public int customRoles(@NotNull Claim claim) {
        return claim.roleRegistry().allRoles().size() - 3;
    }

    public boolean roleLimitReached(@NotNull Claim claim) {
        int max = settingsHolder.get().maxRoles();

        return max >= 0 && customRoles(claim) >= max;
    }

    /**
     * Whether the player may create another role in the claim. Sends the matching message when not
     * and {@code notify} is set.
     */
    public boolean canCreateRole(@NotNull Player player, @NotNull Claim claim, boolean notify) {
        if (!hasPermission(player, claim, Permission.MANAGE_ROLES)) {
            if (notify) {
                deny(player);
            }

            return false;
        }

        if (roleLimitReached(claim)) {
            if (notify) {
                denyRoleLimit(player);
            }

            return false;
        }

        return true;
    }

    /**
     * What the player can do with a role in the roles menu, computed from the current state of the claim.
     */
    public RoleCapabilities roleCapabilities(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimRole role) {
        ClaimMember playerMember = actor(player, claim)
                .orElse(null);
        if (playerMember == null || !roleExists(claim, role)) {
            return RoleCapabilities.NONE;
        }

        ClaimRoleRegistry registry = claim.roleRegistry();
        List<ClaimRole> allRoles = registry.allRoles();
        int allRolesQty = allRoles.size();

        int playerRolePosition = allRoles.indexOf(playerMember.role());
        int rolePosition = allRoles.indexOf(role);

        boolean isOwnerRole = role.equals(registry.ownerRole());
        boolean isDefaultRole = isOwnerRole || role.equals(registry.defaultRole()) || role.equals(registry.everyoneRole());

        boolean canManage = playerMember.hasPermission(Permission.MANAGE_ROLES)
                && playerRolePosition < rolePosition && !isOwnerRole;
        boolean canManagePermissions = playerMember.canEditRolePermissions(role);
        boolean canRename = (playerMember.role().equals(registry.ownerRole()) || canManage) && !role.equals(registry.everyoneRole());
        boolean canSwapPrevious = canManage && rolePosition > 1 && playerRolePosition < rolePosition - 1 && rolePosition < allRolesQty - 2;
        boolean canSwapNext = canManage && rolePosition < allRolesQty - 3;

        return new RoleCapabilities(
                canManage, canManagePermissions, canRename, canSwapPrevious, canSwapNext,
                canManage && !isDefaultRole
        );
    }

    public record RoleCapabilities(
            boolean canManage, boolean canManagePermissions, boolean canRename,
            boolean canSwapPrevious, boolean canSwapNext, boolean canDelete
    ) {

        private final static RoleCapabilities NONE = new RoleCapabilities(false, false, false, false, false, false);
    }

    public void denyRoleLimit(@NotNull Player player) {
        text.send(player, messagesHolder.get().claims().roleLimitReached(), Map.of("max", settingsHolder.get().maxRoles()));
    }

    public void deny(@NotNull Player player) {
        text.send(player, messagesHolder.get().commands().notYourClaim());
    }
}
