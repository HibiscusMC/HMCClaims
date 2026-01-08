package com.hibiscusmc.hmcclaims.claim;

import lombok.Data;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Claim {

    private final UUID claimId;
    private final ClaimMember owner;

    private final ClaimRegion region;
    private final Set<ClaimMember> members;
    private final Set<Claim> children;

    private String name;
    private Claim parent;

    private boolean inheritPermissions;
    private boolean locked;

    private Instant claimedTimestamp;

    public Claim(UUID claimId, Player owner, ClaimRegion region) {
        this.claimId = claimId;
        this.owner = new ClaimMember(owner.getUniqueId(), this, ClaimMemberRole.OWNER);
        this.region = region;

        this.name = owner.getName() + "'s Claim";
        this.members = new HashSet<>();
        this.children = new HashSet<>();
        this.locked = false;
        this.claimedTimestamp = Instant.now();
    }
}