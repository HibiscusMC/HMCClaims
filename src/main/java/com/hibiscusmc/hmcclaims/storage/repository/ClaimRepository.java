package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClaimRepository {

    CompletableFuture<List<Claim>> getAllClaims();

    CompletableFuture<Claim> getClaim(UUID claimUuid);

    CompletableFuture<Void> saveClaim(Claim claim);

    CompletableFuture<Void> deleteClaim(UUID claimUuid);

    CompletableFuture<Void> saveMembers(UUID claimUuid, Collection<ClaimMember> members);

    CompletableFuture<Void> saveSettings(UUID claimUuid, Map<String, String> settings);
}