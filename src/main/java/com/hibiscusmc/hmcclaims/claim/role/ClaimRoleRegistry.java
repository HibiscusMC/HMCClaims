package com.hibiscusmc.hmcclaims.claim.role;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Getter
    private final ClaimRole everyoneRole;

    private final List<ClaimRole> roles = new ArrayList<>();

    /**
     * Constructs a new ClaimRoleRegistry using a provided list of roles.
     * <p>
     * The first element is assigned as the {@link #ownerRole}, the penultimate element
     * is assigned as the {@link #defaultRole}, the last element is assigned as the
     * {@link #everyoneRole} and all elements in between are added to the
     * internal {@link #roles} collection.
     * </p>
     *
     * @param roles the list of roles to initialize the registry with;
     *              must contain at least 3 elements (Owner, Default and Everyone).
     * @throws IllegalArgumentException if the roles list has fewer than 3 elements.
     * @throws NullPointerException     if the roles list is null.
     */
    public ClaimRoleRegistry(@NotNull List<ClaimRole> roles) {
        if (roles.size() < 3) {
            throw new IllegalArgumentException("Registry requires at least an Owner role, a Default role and an Everyone role.");
        }

        List<ClaimRole> clonedRoles = new ArrayList<>(roles)
                .stream()
                .map(role -> {
                    ClaimRole mappedRole = role;

                    if (mappedRole.id() == null) {
                        mappedRole = new ClaimRole(UUID.randomUUID(), role.name(), role.permissions());
                    }

                    mappedRole.position(roles.indexOf(role));

                    return mappedRole;
                })
                .sorted(Comparator.comparingInt(ClaimRole::position))
                .toList();

        this.ownerRole = clonedRoles.getFirst();
        this.defaultRole = clonedRoles.get(clonedRoles.size() - 2);
        this.everyoneRole = clonedRoles.getLast();

        // Adds all roles between the first and last elements
        this.roles.addAll(clonedRoles.subList(1, clonedRoles.size() - 2));
    }

    /**
     * Adds a new role to the registry's collection of non-fixed roles.
     *
     * @param role the {@link ClaimRole} to be added.
     * @throws IllegalArgumentException if the role is the owner, default or everyone role.
     */
    public void add(@NotNull ClaimRole role) {
        if (role.equals(ownerRole) || role.equals(defaultRole) || role.equals(everyoneRole)) {
            throw new IllegalArgumentException("Can't add the fixed default roles to the dynamic list.");
        }

        roles.add(role);
    }

    /**
     * Finds a role by its unique identifier.
     *
     * @param id the unique string identifier of the role.
     * @return an {@link Optional} containing the matching {@link ClaimRole},
     * or an empty Optional if no match is found.
     */
    public @NotNull Optional<ClaimRole> find(@NotNull UUID id) {
        if (ownerRole.id().equals(id)) return Optional.of(ownerRole);
        if (defaultRole.id().equals(id)) return Optional.of(defaultRole);
        if (everyoneRole.id().equals(id)) return Optional.of(everyoneRole);

        return roles.stream()
                .filter(role -> role.id().equals(id))
                .findFirst();
    }

    /**
     * Removes a specific role from the registry.
     * <p>
     * This method prevents the removal of the protected {@link #ownerRole},
     * {@link #defaultRole} and {@link #everyoneRole}.
     * </p>
     *
     * @param role the {@link ClaimRole} to remove.
     * @throws IllegalStateException if the specified role is the owner or default role.
     */
    public void remove(@NotNull ClaimRole role) {
        if (role.equals(ownerRole) || role.equals(defaultRole) || role.equals(everyoneRole)) {
            throw new IllegalStateException("Can't remove default roles");
        }

        roles.remove(role);
    }

    /**
     * Swaps the positions of two roles within the registry.
     * <p>
     * This method only swaps roles that exist within the dynamic {@code roles} list.
     * If either role is not found, or if the provided roles are the protected
     * {@link #ownerRole}, {@link #defaultRole} or {@link #everyoneRole}, the swap will not be performed.
     * </p>
     *
     * @param roleA the first role to swap.
     * @param roleB the second role to swap.
     * @throws IllegalArgumentException if either role is not present in the registry list.
     */
    public void swap(@NotNull ClaimRole roleA, @NotNull ClaimRole roleB) {
        int indexA = roles.indexOf(roleA);
        int indexB = roles.indexOf(roleB);

        if (indexA == -1 || indexB == -1) {
            throw new IllegalArgumentException("Both roles must exist in the dynamic roles list to be swapped.");
        }

        Collections.swap(roles, indexA, indexB);
    }

    /**
     * Retrieves a list of claim roles that a specific role is permitted to manage.
     * <p>
     * The hierarchy dictates that a role can only manage roles positioned hierarchically
     * above it (which corresponds to a higher index in the internal roles tracking, i.e.,
     * closer to the default/everyone tiers). The {@link #ownerRole} bypasses authority checks
     * and can manage all intermediate roles, while the {@link #defaultRole} and {@link #everyoneRole}
     * are unauthorized to manage any roles.
     * </p>
     *
     * @param role the {@link ClaimRole} whose management capabilities are being evaluated
     * @return an unmodifiable {@link List} of {@link ClaimRole}s that the given role can manage;
     * an empty list if the role lacks permission, is a static lowest-tier role, or no higher
     * manageable roles exist.
     */
    @Contract(pure = true)
    public List<ClaimRole> manageableRoles(ClaimRole role) {
        if (role.equals(ownerRole)) {
            return Collections.unmodifiableList(roles);
        }

        if (role.equals(defaultRole) || role.equals(everyoneRole) || !role.hasPermission(Permission.MANAGE_MEMBER_ROLES)) {
            return Collections.emptyList();
        }

        int roleIndex = roles.indexOf(role);
        if (roleIndex == -1) {
            return Collections.emptyList();
        }

        // order (from lowest to highest)
        // owner -> [middle roles] -> default -> everyone
        return roles.stream()
                .filter(other -> roles.indexOf(other) > roleIndex)
                .toList();
    }

    /**
     * Returns all roles in hierarchy order.
     *
     * <p>The returned list is ordered from <b>the highest authority to the lowest authority</b>:
     * <pre>
     * owner -> [middle roles] -> default -> everyone
     * </pre>
     *
     * @return an unmodifiable list of all roles in the hierarchy.
     */
    @Contract(pure = true)
    public List<ClaimRole> allRoles() {
        List<ClaimRole> ordered = new ArrayList<>();

        ordered.add(ownerRole);
        ordered.addAll(roles);
        ordered.add(defaultRole);
        ordered.add(everyoneRole);

        return Collections.unmodifiableList(ordered);
    }
}