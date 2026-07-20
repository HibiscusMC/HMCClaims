package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.players.NameAndId;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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

    @Setter
    private String name;

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
     * The id of the current owner of the claim.
     */
    @Setter
    private UUID owner;

    /**
     * The local authority for managing roles and permissions within this claim.
     */
    @Setter
    @MonotonicNonNull
    private ClaimRoleRegistry roleRegistry;

    private final Map<Setting<?>, SettingHolder<?>> settings = new HashMap<>();

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

    /**
     * If {@code true}, players without bypass permissions cannot interact
     * regardless of their individual roles.
     */
    @Setter
    private boolean locked;

    private final Instant claimedTimestamp;

    @Getter
    private final transient long ttl = System.currentTimeMillis();

    /**
     * Creates a new Claim and initializes the owner with full permissions.
     *
     * @param claimId Unique identifier for this claim.
     * @param main    The main this claim belongs to. {@code null} if none
     * @param owner   The player who is creating and owns the claim.
     * @param region  The physical boundaries of the claim.
     * @param roles   The initial role hierarchy (passed to {@link ClaimRoleRegistry}).
     */
    public Claim(@NotNull UUID claimId, @NotNull String name, @Nullable Claim main, @NotNull NameAndId owner, @NotNull ClaimRegion region, @Nullable List<ClaimRole> roles, @Nullable Instant claimedTimestamp) {
        this.claimId = claimId;
        this.region = region;
        this.main = main;

        this.name = TextUtil.strip(name);
        this.owner = owner.id();

        if (roles != null) {
            this.roleRegistry = new ClaimRoleRegistry(roles);

            if (!owner.name().isEmpty()) {
                addMember(new ClaimMember(
                        owner.id(),
                        this,
                        owner.name(),
                        roleRegistry.ownerRole(),
                        new HashSet<>()
                ));
            }
        }

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

        if (id.equals(owner)) {
            return false;
        }

        ClaimMember oldOwner = members.get(owner);
        ClaimMember member;

        if (members.containsKey(id)) {
            member = members.get(id);

            member.role(roleRegistry.ownerRole());
            member.permissions(new HashSet<>());

            if (member.banned()) {
                member.banned(false);
            }
        } else {
            member = new ClaimMember(
                    id,
                    this,
                    name,
                    roleRegistry.ownerRole(),
                    new HashSet<>()
            );

            members.put(id, member);
        }

        owner = member.uuid();
        oldOwner.role(roleRegistry.defaultRole());
        oldOwner.permissions(new HashSet<>());

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

        this.name = TextUtil.strip(newName);
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
     * @return An unmodifiable view of both banned and non banned members.
     */
    @NotNull
    public Set<ClaimMember> allMembers() {
        return Set.copyOf(members.values());
    }

    /**
     * Adds an already constructed member to the claim.
     *
     * @param member The {@link ClaimMember} to add
     */
    public ClaimMember addMember(@NotNull ClaimMember member) {
        if (members.containsKey(member.uuid())) {
            return null;
        }

        members.put(member.uuid(), member);
        return member;
    }

    /**
     * Constructs a default member and adds it to the claim.
     *
     * @param data The {@link NameAndId} of the player to add
     */
    public ClaimMember addMember(@NotNull NameAndId data) {
        ClaimMember member = new ClaimMember(
                data.id(),
                this,
                data.name(),
                roleRegistry.defaultRole(),
                new HashSet<>()
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

    /**
     * Deflates this {@link Claim} instance into a lightweight, data-only {@link RawClaim} snapshot.
     *
     * @return A compiled {@link RawClaim} payload capturing the current data state.
     */
    @NotNull
    public RawClaim deflate() {
        byte[] serializedRoles = ClaimSerializer.serialize(roleRegistry);
        byte[] serializedMembers = ClaimSerializer.serialize(allMembers());
        byte[] serializedSettings = ClaimSerializer.serialize(settings);

        String worldName = this.region.worldName();
        int minX = this.region.minX();
        int maxX = this.region.maxX();
        int minZ = this.region.minZ();
        int maxZ = this.region.maxZ();

        UUID parentId = this.main != null ? this.main.claimId() : null;
        Set<RawClaim> deflatedSubClaims = new HashSet<>();
        for (Claim subClaim : subClaims) {
            deflatedSubClaims.add(subClaim.deflate());
        }

        return new RawClaim(
                this.claimId, this.owner, this.name, parentId, deflatedSubClaims,
                worldName, minX, maxX, minZ, maxZ,
                serializedRoles, serializedMembers, serializedSettings,
                this.locked, this.claimedTimestamp
        );
    }
}