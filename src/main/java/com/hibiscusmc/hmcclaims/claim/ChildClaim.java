package com.hibiscusmc.hmcclaims.claim;

import org.bukkit.entity.Player;

import java.util.UUID;

public class ChildClaim extends Claim {

    private Claim parent;

    private boolean inheritsPermissions;

    public ChildClaim(UUID claimId, Player owner, ClaimRegion region) {
        super(claimId, owner, region);
    }
}