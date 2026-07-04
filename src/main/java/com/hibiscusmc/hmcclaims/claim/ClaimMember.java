package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player's membership within a {@link Claim}.
 *
 * <p>A claim member has a role, optional individual permission overrides,
 * a join timestamp, and a ban state.</p>
 *
 * <p>This class models the relationship between a player and a claim,
 * not the player entity itself.</p>
 */
@Data
@EqualsAndHashCode(of = {"uuid", "claim"})
public class ClaimMember {

    private final UUID uuid;
    private final Claim claim;
    private final String lastKnownName;

    private ClaimRole role;

    /**
     * Explicit permission overrides for this member.
     *
     * <p>Resolution order:</p>
     * <ol>
     *     <li>If an explicit override exists, its value is used.</li>
     *     <li>Otherwise, the member's role permissions are used.</li>
     * </ol>
     *
     * <p>A value of {@code true} explicitly grants the permission,
     * while {@code false} explicitly denies it.</p>
     */
    private Set<PermissionHolder> permissions;

    /**
     * Whether this member is currently banned from the claim.
     * A banned member cannot interact with the claim.
     */
    private boolean banned;

    @Setter
    private Instant joinedTimestamp;

    public ClaimMember(UUID uuid, Claim claim, String lastKnownName, ClaimRole role, Set<PermissionHolder> permissions) {
        this.uuid = uuid;
        this.claim = claim;
        this.lastKnownName = lastKnownName;

        this.role = role;
        this.permissions = permissions;

        this.banned = false;
        this.joinedTimestamp = Instant.now();
    }

    /**
     * Returns whether this member is able to manage the member passed as a parameter.
     *
     * @param other The other {@link ClaimMember} to check
     * @return {@code true} if this member can manage it.
     */
    public boolean canManage(ClaimMember other) {
        if (uuid.equals(other.uuid)) {
            return false;
        }

        ClaimRoleRegistry registry = claim.roleRegistry();
        if (other.role.equals(registry.ownerRole())) {
            return false;
        }

        if (!hasPermission(Permission.MANAGE_MEMBER_ROLES) && !hasPermission(Permission.MANAGE_MEMBER_PERMISSIONS)) {
            return false;
        }

        List<ClaimRole> roles = registry.allRoles();

        int roleIndex = roles.indexOf(role);
        int otherRoleIndex = roles.indexOf(other.role);

        return roleIndex < otherRoleIndex;
    }

    /**
     * Returns whether this member has the specific permission.
     *
     * @param permission The {@link Permission} to check for
     * @return {@code true} if this member has the permission
     */
    public boolean hasPermission(Permission permission) {
        if (claim.owner().equals(this.uuid)) {
            return true;
        }

        PermissionHolder holder = permissions.stream().filter(h -> h.permission().equals(permission))
                .findAny()
                .orElse(null);

        if (holder == null) {
            return role.hasPermission(permission);
        }

        return holder.status();
    }
}