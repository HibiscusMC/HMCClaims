package com.hibiscusmc.hmcclaims.storage.repository.sql;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.impl.remote.HikariStorage;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SQLClaimRepository implements ClaimRepository {

    private final Settings.Storage settings;
    private final Storage storage;
    private final ExecutorService executor;

    public SQLClaimRepository(Settings.Storage settings, HikariStorage storage, ExecutorService executor) {
        this.settings = settings;
        this.storage = storage;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<Claim>> getAllClaims() {
        return null;
    }

    @Override
    public CompletableFuture<Claim> getClaim(UUID claimUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveClaim(Claim claim) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteClaim(UUID claimUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveMembers(UUID claimUuid, Collection<ClaimMember> members) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveSettings(UUID claimUuid, Map<String, String> settings) {
        return null;
    }
}