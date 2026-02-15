package com.hibiscusmc.hmcclaims.claim.role;

import com.hibiscusmc.hmcclaims.claim.ClaimPermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a role within a claim.
 *
 * <p>A role has a unique identifier and defines a set of
 * permissions granted to members assigned to it.
 *
 * <p>Equality is based solely on the role's unique id.
 * Instances are immutable.
 */
@Getter
@EqualsAndHashCode(of = {"id"})
public class ClaimRole {

    private final String name;

    private final UUID id;

    private final Set<ClaimPermission> permissions;

    /**
     * Creates a new immutable claim role.
     *
     * @param name        the display name of the role
     * @param id          the unique identifier of the role
     * @param permissions the permissions granted to this role
     * @throws NullPointerException if any argument is null
     */
    public ClaimRole(String name, UUID id, Set<ClaimPermission> permissions) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.id = Objects.requireNonNull(id, "id cannot be null");

        this.permissions = Collections.unmodifiableSet(
                EnumSet.copyOf(permissions)
        );
    }

    /**
     * Checks whether this role has the given permission.
     *
     * @param permission the permission to check
     * @return <code>true</code> if the role has the permission, <code>false</code> otherwise
     */
    public boolean hasPermission(ClaimPermission permission) {
        return permissions.contains(permission);
    }
}