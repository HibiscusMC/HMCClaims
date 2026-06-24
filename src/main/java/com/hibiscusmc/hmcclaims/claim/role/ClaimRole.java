package com.hibiscusmc.hmcclaims.claim.role;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 */
@Getter
@EqualsAndHashCode(of = {"name", "id"})
public class ClaimRole {

    @MonotonicNonNull
    @Setter
    private UUID id;
    private String name;

    private final Set<Permission> permissions;

    @Setter
    private int position = -1;

    /**
     * Creates a new immutable claim role.
     *
     * @param id          the unique identifier of the role
     * @param name        the display name of the role
     * @param permissions the permissions granted to this role
     * @throws NullPointerException if any argument is null
     */
    public ClaimRole(@Nullable UUID id, String name, Set<Permission> permissions) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name cannot be null");

        this.permissions = permissions;
    }

    /**
     * Changes the name of the role
     *
     * @param newName the new name of the role
     */
    public void rename(@NotNull String newName) {
        if (newName.isEmpty()) {
            throw new IllegalArgumentException("role name cannot be empty");
        }

        if (newName.length() < 2) {
            throw new IllegalArgumentException("role name cannot be less than 2 characters");
        }

        if (newName.length() > 48) {
            throw new IllegalArgumentException("role name cannot be longer than 48 characters");
        }

        if (newName.equals(this.name)) {
            return;
        }

        this.name = newName;
    }

    /**
     * Adds a specific permission to the role
     *
     * @param permission the permission to add
     */
    public void addPermission(@NotNull Permission permission) {
        if (permissions.contains(permission)) {
            return;
        }

        permissions.add(permission);
    }

    /**
     * Checks whether this role has the given permission.
     *
     * @param permission the permission to check
     * @return <code>true</code> if the role has the permission, <code>false</code> otherwise
     */
    public boolean hasPermission(@NotNull Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * Removes a specific permission from the role
     *
     * @param permission the permission to remove
     */
    public void removePermission(@NotNull Permission permission) {
        permissions.remove(permission);
    }
}