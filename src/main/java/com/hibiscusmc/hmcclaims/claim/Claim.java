package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.players.NameAndId;
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
     * The spatial bounds defining where this claim exists in the world.
     */
    @Setter
    private ClaimRegion region;

    /**
     * The chunks this claim occupies.
     */
    @Setter
    private LongSet chunks;

    /**
     * The current owner of the claim.
     */
    private ClaimMember owner;

    /**
     * The local authority for managing roles and permissions within this claim.
     */
    private ClaimRoleRegistry roleRegistry;

    private final Map<String, SettingHolder<?>> settings = new HashMap<>();

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
    public Claim(@NotNull UUID claimId, @NotNull String name, @Nullable Claim main, @NotNull NameAndId owner, @NotNull ClaimRegion region, @NotNull List<ClaimRole> roles, @Nullable Instant claimedTimestamp) {
        this.claimId = claimId;
        this.region = region;
        this.main = main;
        this.roleRegistry = new ClaimRoleRegistry(roles);

        this.name = name;

        UUID uuid = owner.id();
        this.owner = new ClaimMember(
                uuid,
                this,
                owner.name(),
                roleRegistry.ownerRole(),
                PermissionRegistry.getAllPermissions()
                        .stream()
                        .map(permission -> new PermissionHolder(permission, true))
                        .collect(Collectors.toUnmodifiableSet())
        );
        addMember(this.owner);

        this.locked = false;

        if (claimedTimestamp != null) {
            this.claimedTimestamp = claimedTimestamp;
        } else {
            this.claimedTimestamp = Instant.now();
        }


    }

    /**
     * Convenience constructor that generates a default name based on ownership.
     */
    public Claim(@NotNull UUID claimId, @Nullable Claim main, @NotNull NameAndId owner, @NotNull ClaimRegion region, @NotNull List<ClaimRole> roles, int totalClaims) {
        this(claimId,
                main == null ? owner.name() + "'s Claim " + totalClaims : "Sub Claim of " + main.name(),
                main, owner, region, roles, null);
    }

    /**
     * Inherits every member, role and permission from the main claim
     *
     * @throws IllegalStateException if the claim is not a sub claim
     */
    public void inheritPermissions() {
        if (main == null) {
            throw new IllegalStateException("A main claim can't inherit permissions");
        }

        ClaimRoleRegistry mainRegistry = main.roleRegistry();

        roleRegistry = new ClaimRoleRegistry(mainRegistry.allRoles());

        List<ClaimRole> roles = roleRegistry.allRoles();
        members.clear();

        long start = 0;
        for (ClaimMember member : main.members()) {
            ClaimMember newMember = new ClaimMember(
                    member.uuid(),
                    this,
                    member.lastKnownName(),
                    roles.stream().filter(role -> role.id().equals(member.role().id()))
                            .findAny()
                            .orElse(roleRegistry.defaultRole()),
                    member.permissions()
            );
            newMember.banned(member.banned());
            newMember.joinedTimestamp(member.joinedTimestamp().plusMillis(start++));

            members.put(member.uuid(), newMember);
        }
    }

    /**
     * Transfer the claim ownership to another player.
     *
     * @param newOwner The player that will be the new owner of the claim
     * @return {@code false} if the new owner is the same as the current one, {@code true} otherwise.
     */
    public boolean transfer(@NotNull NameAndId newOwner) {
        String name = newOwner.name();
        UUID id = newOwner.id();

        if (id.equals(owner.uuid())) {
            return false;
        }

        ClaimMember oldOwner = members.get(owner.uuid());
        ClaimMember member;
        Set<PermissionHolder> allPermissions = PermissionRegistry.getAllPermissions()
                .stream()
                .map(permission -> new PermissionHolder(permission, true))
                .collect(Collectors.toUnmodifiableSet());

        if (members.containsKey(id)) {
            member = members.get(id);

            member.role(roleRegistry.ownerRole());
            member.permissions(allPermissions);

            if (member.banned()) {
                member.banned(false);
            }
        } else {
            member = new ClaimMember(
                    id,
                    this,
                    name,
                    roleRegistry.ownerRole(),
                    allPermissions
            );

            members.put(id, member);
        }

        owner = member;
        oldOwner.role(roleRegistry.defaultRole());
        oldOwner.permissions(roleRegistry.defaultRole().permissions()
                .stream().map(permission -> new PermissionHolder(permission, true))
                .collect(Collectors.toUnmodifiableSet()));

        if (!subClaims.isEmpty()) {
            subClaims.forEach(subClaim -> subClaim.transfer(newOwner));
        }

        return true;
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
    public Set<ClaimMember> members() {
        return members.values().stream()
                .filter(member -> !member.banned())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return An unmodifiable view of the current banned members.
     */
    @NotNull
    public Set<ClaimMember> bannedMembers() {
        return members.values().stream()
                .filter(ClaimMember::banned)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Adds an already constructed member to the claim.
     *
     * @param member The {@link ClaimMember} to add
     */
    public boolean addMember(@NotNull ClaimMember member) {
        if (members.containsKey(member.uuid())) {
            return false;
        }

        members.put(member.uuid(), member);
        return true;
    }

    /**
     * Constructs a default member and adds it to the claim.
     *
     * @param data The {@link NameAndId} of the player to add
     */
    public boolean addMember(@NotNull NameAndId data) {
        ClaimMember member = new ClaimMember(
                data.id(),
                this,
                data.name(),
                roleRegistry.defaultRole(),
                roleRegistry.defaultRole().permissions()
                        .stream().map(permission -> new PermissionHolder(permission, true))
                        .collect(Collectors.toUnmodifiableSet())
        );

        return addMember(member);
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
     * @param uuid the uuid of the member to remove
     */
    public boolean removeMember(@NotNull UUID uuid) {
        ClaimMember oldMember = members.remove(uuid);

        return oldMember != null;
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
     * @throws IllegalStateException If trying to add the claim to itself or adding to another sub claim.
     */
    public void addSubClaim(@NotNull Claim claim) {
        if (main != null) {
            throw new IllegalStateException("You can't add a sub claim to another sub claim");
        }

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