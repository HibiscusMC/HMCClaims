package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles complex persistence and relational data mapping for Claims.
 */
public interface ClaimRepository {

    /**
     * Loads all claims from the database.
     */
    @NotNull
    CompletableFuture<List<Claim>> getAllClaims();

    /**
     * Retrieves a single claim with all its relational data (members, roles, etc.).
     */
    @NotNull
    CompletableFuture<Claim> getClaim(@NotNull UUID claimUuid);

    /**
     * Persists the core claim data to the {@code claims} table.
     */
    @NotNull
    CompletableFuture<Void> saveClaim(@NotNull Claim claim);

    /**
     * Deletes a claim.
     */
    @NotNull
    CompletableFuture<Void> deleteClaim(@NotNull UUID claimUuid);

    /**
     * Syncs the members of a claim.
     */
    @NotNull
    CompletableFuture<Void> saveMembers(@NotNull UUID claimUuid, @NotNull Collection<ClaimMember> members);

    /**
     * Updates the custom settings (key-value pairs) for a specific claim.
     */
    @NotNull
    CompletableFuture<Void> saveSettings(@NotNull UUID claimUuid, @NotNull Map<String, String> settings);

    /**
     * Syncs the permission overrides for a specific player within a claim.
     */
    @NotNull
    CompletableFuture<Void> savePermissions(@NotNull UUID claimUuid, @NotNull UUID playerUuid, @NotNull Map<String, Boolean> permissions);

    /**
     * Persists the custom roles defined within a specific claim.
     */
    @NotNull
    CompletableFuture<Void> saveRoles(@NotNull UUID claimUuid, @NotNull Collection<ClaimRole> roles);
}