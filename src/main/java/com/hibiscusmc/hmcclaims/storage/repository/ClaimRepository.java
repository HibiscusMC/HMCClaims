package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionHolder;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    CompletableFuture<List<Claim>> getAllClaims(long chunkKey);

    /**
     * Loads all sub-claims from the database.
     */
    @NotNull
    CompletableFuture<List<Claim>> getAllSubClaims(@NotNull Claim mainClaim);

    /**
     * Retrieves a single claim with all its relational data (members, roles, etc.).
     */
    @NotNull
    CompletableFuture<Claim> getClaim(@NotNull UUID claimUuid);

    /**
     * Retrieves the members of the specified claim.
     */
    @NotNull
    CompletableFuture<List<ClaimMember>> getClaimMembers(@NotNull Claim claim);

    /**
     * Retrieves the permissions of the specified member
     */
    @NotNull
    CompletableFuture<Set<PermissionHolder>> getClaimMemberPermissions(@NotNull UUID claimId, @NotNull UUID playerId);

    /**
     * Deletes a claim.
     */
    @NotNull
    CompletableFuture<Void> deleteClaim(@NotNull UUID claimUuid);

    /**
     * Persists the core claim data to the {@code claims} table.
     */
    @NotNull
    CompletableFuture<Void> saveClaim(@NotNull Claim claim);

    /**
     * Persists the claim chunks to the {@code claims} table.
     */
    @NotNull
    CompletableFuture<Void> saveClaimChunks(@NotNull Claim claim);

    /**
     * Syncs the members of a claim.
     */
    @NotNull
    CompletableFuture<Void> saveMembers(@NotNull UUID claimUuid, @NotNull Collection<ClaimMember> members);

    /**
     * Updates the custom settings (key-value pairs) for a specific claim.
     */
    @NotNull
    CompletableFuture<Void> saveSettings(@NotNull UUID claimUuid, @NotNull Map<String, SettingHolder<?>> settings);

    /**
     * Syncs the permission overrides for a specific player within a claim.
     */
    @NotNull
    CompletableFuture<Void> savePermissions(@NotNull UUID claimUuid, @NotNull UUID playerUuid, @NotNull Map<String, Boolean> permissions);

    /**
     * Persists the custom roles defined within a specific claim.
     */
    @NotNull
    CompletableFuture<Void> saveRoles(@NotNull UUID claimUuid, @NotNull List<ClaimRole> roles);
}