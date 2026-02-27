package com.hibiscusmc.hmcclaims.storage.repository.sql;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.impl.remote.HikariStorage;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull CompletableFuture<List<Claim>> getAllClaims() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Claim> getClaim(@NotNull UUID claimUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveClaim(@NotNull Claim claim) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteClaim(@NotNull UUID claimUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveMembers(@NotNull UUID claimUuid, @NotNull Collection<ClaimMember> members) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveSettings(@NotNull UUID claimUuid, @NotNull Map<String, String> settings) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> savePermissions(@NotNull UUID claimUuid, @NotNull UUID playerUuid, @NotNull Map<String, Boolean> permissions) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveRoles(@NotNull UUID claimUuid, @NotNull Collection<ClaimRole> roles) {
        return CompletableFuture.completedFuture(null);
    }
}