package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.permission.PermissionRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
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
 * Represents a protected region of the world with associated members and permissions.
 * <p>
 * A claim manages its own spatial boundaries (region), its hierarchy of roles,
 * and its relationship to parent or child claims.
 * </p>
 */
@Getter
@EqualsAndHashCode(of = {"claimId"})
public class Claim {

    private final UUID claimId;

    private final ClaimMember owner;
    private final ClaimRegion region;

    @Getter(AccessLevel.NONE)
    private final Map<UUID, ClaimMember> members = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Set<Claim> children = new HashSet<>();

    private final ClaimRoleRegistry roleRegistry;

    @Nullable
    private final Claim parent;

    private final Instant claimedTimestamp;

    @Setter
    private String name;
    @Setter
    private boolean inheritPermissions;
    @Setter
    private boolean locked;

    public Claim(UUID claimId, @Nullable Claim parent, Player owner, ClaimRegion region, List<ClaimRole> roles, int totalClaims) {
        this(claimId,
                parent == null ? owner.getName() + "'s Claim " + totalClaims : "Child of " + parent.name(),
                parent, owner, region, roles);
    }

    /**
     * Creates a new Claim and initializes the owner with full permissions.
     *
     * @param claimId Unique identifier for this claim.
     * @param parent  The parent this claim belongs to. {@code null} if none
     * @param owner   The player who is creating and owns the claim.
     * @param region  The physical boundaries of the claim.
     * @param roles   The initial role hierarchy (passed to {@link ClaimRoleRegistry}).
     */
    public Claim(UUID claimId, String name, @Nullable Claim parent, Player owner, ClaimRegion region, List<ClaimRole> roles) {
        this.claimId = claimId;
        this.region = region;
        this.parent = parent;
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

    public Map<UUID, ClaimMember> members() {
        return Collections.unmodifiableMap(members);
    }

    public void addMember(ClaimMember member) {
        members.put(member.uuid(), member);
    }

    public Optional<ClaimMember> getMember(UUID uuid) {
        return Optional.ofNullable(members.get(uuid));
    }

    public void removeMember(ClaimMember member) {
        members.remove(member.uuid());
    }

    public Set<Claim> children() {
        return Collections.unmodifiableSet(children);
    }

    public void addChild(Claim claim) {
        if (claim.equals(this)) {
            throw new IllegalStateException("This claim can't be a child of itself");
        }

        children.add(claim);
    }

    public void removeChild(Claim claim) {
        children.remove(claim);
    }
}