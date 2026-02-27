package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.permission.PermissionRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a region in the world with a defined set of permissions.
 * <p>
 * A claim can be either a standalone "Main" claim or a "Sub-claim" nested within
 * a parent. It manages its own set of members, a local role hierarchy via
 * {@link ClaimRoleRegistry}, and physical boundaries.
 */
@Getter
@EqualsAndHashCode(of = {"claimId"})
public class Claim {

    /**
     * The unique persistent identifier for this claim instance.
     */
    private final UUID claimId;

    /**
     * The primary authority and creator of this claim.
     */
    private final ClaimMember owner;

    /**
     * The spatial bounds defining where this claim exists in the world.
     */
    private final ClaimRegion region;

    /**
     * The local authority for managing roles and permissions within this claim.
     */
    private final ClaimRoleRegistry roleRegistry;

    @Getter(AccessLevel.NONE)
    private final Map<UUID, ClaimMember> members = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Set<Claim> subClaims = new HashSet<>();

    /**
     * The parent claim if this is a sub-claim
     * <p>
     * If {@code null}, this is a top-level "main" claim.
     */
    @Nullable
    private final Claim main;

    private final Instant claimedTimestamp;

    @Setter
    private String name;

    /**
     * Whether this claim should automatically inherit permissions from its
     * parent ({@link #main}) claim.
     */
    @Setter
    private boolean inheritPermissions;

    /**
     * If {@code true}, players without bypass permissions cannot interact
     * regardless of their individual roles.
     */
    @Setter
    private boolean locked;

    /**
     * Creates a new Claim and initializes the owner with full permissions.
     *
     * @param claimId Unique identifier for this claim.
     * @param main    The main this claim belongs to. {@code null} if none
     * @param owner   The player who is creating and owns the claim.
     * @param region  The physical boundaries of the claim.
     * @param roles   The initial role hierarchy (passed to {@link ClaimRoleRegistry}).
     */
    public Claim(@NotNull UUID claimId, @NotNull String name, @Nullable Claim main, @NotNull Player owner, @NotNull ClaimRegion region, @NotNull List<ClaimRole> roles) {
        this.claimId = claimId;
        this.region = region;
        this.main = main;
        this.roleRegistry = new ClaimRoleRegistry(roles);

        this.name = name;

        UUID uuid = owner.getUniqueId();
        this.owner = new ClaimMember(
                uuid,
                this,
                owner.getName(),
                roleRegistry.ownerRole(),
                PermissionRegistry.getAllPermissions()
                        .stream()
                        .map(permission -> new PermissionHolder(permission, true))
                        .collect(Collectors.toUnmodifiableSet())
        );
        addMember(this.owner);

        this.locked = false;
        this.inheritPermissions = true;

        this.claimedTimestamp = Instant.now();
    }

    /**
     * Convenience constructor that generates a default name based on ownership.
     */
    public Claim(@NotNull UUID claimId, @Nullable Claim main, @NotNull Player owner, @NotNull ClaimRegion region, @NotNull List<ClaimRole> roles, int totalClaims) {
        this(claimId,
                main == null ? owner.getName() + "'s Claim " + totalClaims : "Sub Claim of " + main.name(),
                main, owner, region, roles);
    }

    /**
     * Renames the claim
     *
     * @param newName the new claim name
     * @throws IllegalArgumentException if the claim name is not valid (is empty, less than 2 or higher than 48 characters)
     */
    public void rename(@NotNull String newName) {
        if (newName.isEmpty()) {
            throw new IllegalArgumentException("claim name cannot be empty");
        }

        if (newName.length() < 2) {
            throw new IllegalArgumentException("claim name cannot be less than 2 characters");
        }

        if (newName.length() > 48) {
            throw new IllegalArgumentException("claim name cannot be longer than 48 characters");
        }

        if (newName.equals(this.name)) {
            return;
        }

        this.name = newName;
    }

    /**
     * @return An unmodifiable view of the current members.
     */
    @NotNull
    public Map<UUID, ClaimMember> members() {
        return Collections.unmodifiableMap(members);
    }

    /**
     * Adds a member to the claim.
     *
     * @param member the member to add
     */
    public void addMember(@NotNull ClaimMember member) {
        members.put(member.uuid(), member);
    }

    /**
     * Gets a member from the claim.
     *
     * @param uuid the uuid of the member
     */
    @NotNull
    @Contract(pure = true)
    public Optional<ClaimMember> getMember(@NotNull UUID uuid) {
        return Optional.ofNullable(members.get(uuid));
    }

    /**
     * Removes a member to the claim.
     *
     * @param member the member to remove
     */
    public void removeMember(@NotNull ClaimMember member) {
        members.remove(member.uuid());
    }

    /**
     * @return An unmodifiable view of the nested sub-claims.
     */
    @NotNull
    @Contract(pure = true)
    public Set<Claim> subClaims() {
        return Collections.unmodifiableSet(subClaims);
    }

    /**
     * Links a sub-claim to this claim.
     *
     * @param claim The sub-claim to add.
     * @throws IllegalStateException If trying to add the claim to itself.
     */
    public void addSubClaim(@NotNull Claim claim) {
        if (claim.equals(this)) {
            throw new IllegalStateException("This claim can't be a sub claim of itself");
        }

        subClaims.add(claim);
    }

    /**
     * Removes a sub-claim to this claim.
     *
     * @param claim The sub-claim to remove.
     */
    public void removeSubClaim(@NotNull Claim claim) {
        subClaims.remove(claim);
    }
}