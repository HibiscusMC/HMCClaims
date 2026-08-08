package com.hibiscusmc.hmcclaims;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.user.UserManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

@Getter
@Singleton
public class HMCClaimsAPI {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private UserManager userManager;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private static HMCClaimsAPI instance;
}