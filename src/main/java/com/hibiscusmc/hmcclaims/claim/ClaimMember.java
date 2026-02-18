package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
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

    private final ClaimRole role;


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
    private final Set<PermissionHolder> permissions;

    /**
     * Whether this member is currently banned from the claim.
     * A banned member cannot interact with the claim.
     */
    private boolean banned;

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

}