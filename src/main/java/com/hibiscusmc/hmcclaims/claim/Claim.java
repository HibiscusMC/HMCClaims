package com.hibiscusmc.hmcclaims.claim;

import lombok.Data;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Claim {

    private final UUID claimId;

    private final UUID owner;

    private final ClaimRegion region;

    private String name;

    private Set<ClaimMember> members;

    private Set<ChildClaim> childrens;

    private boolean locked;

    private long claimedTimestamp;

    public Claim(UUID claimId, Player owner, ClaimRegion region) {
        this.claimId = claimId;
        this.owner = owner.getUniqueId();
        this.region = region;

        this.name = owner.getName() + "'s Claim";
        this.members = new HashSet<>();
        this.childrens = new HashSet<>();
        this.locked = false;
        this.claimedTimestamp = System.currentTimeMillis();
    }

}