package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.RawClaim;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles complex persistence and relational data mapping for Claims.
 */
public interface ClaimRepository {
    int SCHEMA_VERSION = 1;

    /**
     * Loads all claims from the database.
     */
    @NotNull
    CompletableFuture<RawClaim.CacheHolder> getAllClaims(String worldName);

    /**
     * Retrieves multiple claims with all its relational data (members, roles, etc.).
     */
    @NotNull
    CompletableFuture<List<RawClaim>> getClaims(@NotNull List<UUID> claimIds);

    /**
     * Retrieves a single claim with all its relational data (members, roles, etc.).
     */
    @NotNull
    CompletableFuture<RawClaim> getClaim(@NotNull UUID claimId);

    /**
     * Persists the core claim data to the {@code claims} table.
     */
    @NotNull
    CompletableFuture<Void> saveClaim(@NotNull Claim claim);

    /**
     * Syncs all members of a claim.
     */
    @NotNull
    CompletableFuture<Void> saveMembers(@NotNull Claim claim);

    /**
     * Syncs all roles of a claim.
     */
    @NotNull
    CompletableFuture<Void> saveRoles(@NotNull Claim claim);

    /**
     * Updates the custom settings (key-value pairs) for a specific claim.
     */
    @NotNull
    CompletableFuture<Void> saveSettings(@NotNull Claim claim);

    /**
     * Deletes a claim.
     */
    @NotNull
    CompletableFuture<Void> deleteClaim(@NotNull UUID claimId);
}