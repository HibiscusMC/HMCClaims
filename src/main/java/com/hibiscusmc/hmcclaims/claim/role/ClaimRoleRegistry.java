package com.hibiscusmc.hmcclaims.claim.role;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the ordered role hierarchy of a claim.
 *
 * <p>The hierarchy is ordered by authority, where the first element
 * is the owner (highest authority) and the last is the default (lowest).
 *
 * <p>The registry internally stores only the middle roles.
 * The owner and default roles are fixed and cannot be removed or swapped.
 */
public class ClaimRoleRegistry {

    @Getter
    private final ClaimRole ownerRole;
    @Getter
    private final ClaimRole defaultRole;

    private final List<ClaimRole> roles = new ArrayList<>();

    /**
     * Constructs a new ClaimRoleRegistry using a provided list of roles.
     * <p>
     * The first element is assigned as the {@code ownerRole}, the last element
     * is assigned as the {@code defaultRole}, and all elements in between are
     * added to the internal {@code roles} collection.
     * </p>
     *
     * @param roles the list of roles to initialize the registry with;
     *              must contain at least 2 elements (Owner and Default).
     * @throws IllegalArgumentException if the roles list has fewer than 2 elements.
     * @throws NullPointerException     if the roles list is null.
     */
    public ClaimRoleRegistry(List<ClaimRole> roles) {
        if (roles == null || roles.size() < 2) {
            throw new IllegalArgumentException("Registry requires at least an Owner role and a Default role.");
        }

        this.ownerRole = roles.getFirst();
        this.defaultRole = roles.getLast();

        // Adds all roles between the first and last elements
        this.roles.addAll(roles.subList(1, roles.size() - 1));
    }

    /**
     * Adds a new role to the registry's collection of non-fixed roles.
     *
     * @param role the {@link ClaimRole} to be added.
     * @throws IllegalArgumentException if the role is already the owner or default role.
     */
    public void add(ClaimRole role) {
        if (role.equals(ownerRole) || role.equals(defaultRole)) {
            throw new IllegalArgumentException("Can't add the fixed owner or default role to the dynamic list.");
        }

        roles.add(role);
    }

    /**
     * Removes a specific role from the registry.
     * <p>
     * This method prevents the removal of the protected {@code ownerRole}
     * and {@code defaultRole}.
     * </p>
     *
     * @param role the {@link ClaimRole} to remove.
     * @throws IllegalStateException if the specified role is the owner or default role.
     */
    public void remove(ClaimRole role) {
        if (role.equals(ownerRole) || role.equals(defaultRole)) {
            throw new IllegalStateException("Can't remove default roles");
        }

        roles.remove(role);
    }

    /**
     * Swaps the positions of two roles within the registry.
     * <p>
     * This method only swaps roles that exist within the dynamic {@code roles} list.
     * If either role is not found, or if the provided roles are the protected
     * {@code ownerRole} or {@code defaultRole}, the swap will not be performed.
     * </p>
     *
     * @param roleA the first role to swap.
     * @param roleB the second role to swap.
     * @throws IllegalArgumentException if either role is not present in the registry list.
     */
    public void swap(ClaimRole roleA, ClaimRole roleB) {
        int indexA = roles.indexOf(roleA);
        int indexB = roles.indexOf(roleB);

        if (indexA == -1 || indexB == -1) {
            throw new IllegalArgumentException("Both roles must exist in the dynamic roles list to be swapped.");
        }

        Collections.swap(roles, indexA, indexB);
    }

    /**
     * Returns all roles in hierarchy order.
     *
     * <p>The returned list is ordered from <b>the highest authority to the lowest authority</b>:
     * <pre>
     * owner -> [middle roles] -> default
     * </pre>
     * @return an unmodifiable list of all roles in the hierarchy.
     */
    public List<ClaimRole> getRoles() {
        List<ClaimRole> ordered = new ArrayList<>();
        ordered.add(ownerRole);
        ordered.addAll(roles);
        ordered.add(defaultRole);
        return Collections.unmodifiableList(ordered);
    }
}