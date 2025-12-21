package com.hibiscusmc.hmcclaims.claim;

import java.util.Set;
import java.util.UUID;

public class ClaimMember {

    private UUID uuid;

    private Set<ClaimPermission> permissions;

    private boolean banned;

    private long joinedTimestamp;

}