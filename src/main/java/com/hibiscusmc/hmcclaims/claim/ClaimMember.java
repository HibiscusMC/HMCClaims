package com.hibiscusmc.hmcclaims.claim;

import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class ClaimMember {

    private final UUID uuid;
    private final Claim claim;

    private final ClaimMemberRole role;
    private final Set<ClaimMemberPermission> permissions;

    private boolean banned;

    private Instant joinedTimestamp;

    public ClaimMember(UUID uuid, Claim claim) {
        this(uuid, claim, ClaimMemberRole.MEMBER);
    }

    public ClaimMember(UUID uuid, Claim claim, ClaimMemberRole role) {
        this(uuid, claim, role, Set.of());
    }

    public ClaimMember(UUID uuid, Claim claim, ClaimMemberRole role, Set<ClaimMemberPermission> permissions) {
        this.uuid = uuid;
        this.claim = claim;

        this.role = role;
        this.permissions = permissions;

        this.banned = false;
        this.joinedTimestamp = Instant.now();
    }

}