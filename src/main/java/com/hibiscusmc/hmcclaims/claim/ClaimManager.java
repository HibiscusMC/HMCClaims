package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.api.event.ClaimDeleteEvent;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import com.hibiscusmc.hmcclaims.config.Permissions;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.util.ChunkUtil;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import net.kyori.adventure.key.InvalidKeyException;
import net.minecraft.server.players.NameAndId;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The central authority for managing claim lifecycles and spatial lookups.
 */
@Singleton
public class ClaimManager {

    /**
     * Spatial index: World Name -> Chunk Key (Long) -> List of Claims in that chunk.
     */
    @Getter
    private final Map<String, Long2ObjectMap<Set<Claim>>> worldChunkMaps
            = new ConcurrentHashMap<>();

    /**
     * Maps claims inside a world
     */
    @Getter
    private final Map<String, Set<Claim>> worldClaims
            = new ConcurrentHashMap<>();

    /**
     * Maps Player UUIDs to their owned claims for quick profile lookups.
     */
    private final Map<UUID, Set<Claim>> playerClaims
            = new ConcurrentHashMap<>();

    /**
     * Maps claim UUIDs for quick claim lookups.
     */
    @Getter
    private final Map<UUID, Claim> claims
            = new ConcurrentHashMap<>();

    @Inject
    private ConfigHolder<Permissions> permissionsHolder;

    @Inject
    private ConfigHolder<ClaimSettings> claimSettingsHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private UnloadedClaims unloadedClaims;

    @NotNull
    private ClaimRole defaultRole(@NotNull Permissions.DefaultRole role) {
        Set<Permission> permissions = new HashSet<>();

        for (String id : role.permissions()) {
            if (id.equals("*")) {
                permissions.addAll(PermissionRegistry.getAllPermissions());
                continue;
            }

            Permission permission = findPermission(id);
            if (permission == null) {
                Logger.warning("Ignoring unknown permission '{}' in the default role '{}'", id, role.name());
                continue;
            }

            permissions.add(permission);
        }

        return new ClaimRole(null, role.name(), permissions);
    }

    @Nullable
    private Permission findPermission(@NotNull String id) {
        try {
            return PermissionRegistry.getPermission(id);
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    /**
     * Queues a save of every loaded claim, snapshotting each one on the calling thread.
     */
    public void saveAll() {
        ClaimRepository repository = storageHolder.get().claims();

        for (Claim claim : claims.values()) {
            repository.saveClaim(claim);
        }
    }

    /**
     * Creates a new claim, registers it in the cache, and updates user claim blocks.
     *
     * @param user The owner of the claim.
     * @param region The physical bounds.
     * @param main   The main claim, if creating a sub-claim.
     * @return The newly created {@link Claim} instance.
     */
    @Contract("_, _, _ -> new")
    public Claim createClaim(@NotNull User user, @NotNull ClaimRegion region, @Nullable Claim main) {
        long totalMainClaims = playerClaims.getOrDefault(user.uuid(), new HashSet<>())
                .stream()
                .filter(claim -> claim.main() == null)
                .count();

        Permissions.Roles roles = permissionsHolder.get().roles();
        ClaimSettings claimSettings = claimSettingsHolder.get();

        Claim newClaim = new Claim(
                UUID.randomUUID(),
                main,
                new NameAndId(user.uuid(), user.lastKnownName()),
                region,
                new ArrayList<>(List.of(
                        defaultRole(roles.owner()),
                        defaultRole(roles.member()),
                        defaultRole(roles.everyone())
                )),
                (int) totalMainClaims + 1
        );

        for (Setting<?> registered : SettingRegistry.getAllSettings()) {
            //noinspection unchecked
            Setting<Object> setting = (Setting<Object>) registered;
            SettingHolder<Object> holder = SettingHolder.from(setting);
            holder.value(claimSettings.defaultValue(setting));

            newClaim.settings().put(setting, holder);
        }

        if (main != null) {
            main.addSubClaim(newClaim);
        }

        addClaimToCache(newClaim);

        Storage storage = storageHolder.get();

        ClaimRepository claimRepository = storage.claims();
        if (!user.persistent()) {
            RawClaim snapshot = newClaim.deflate();

            CompletableFuture<Void> userSave = storage.users().saveUser(user);
            userSave.thenRun(() -> user.persistent(true));

            claimRepository.saveClaim(snapshot, userSave);
        } else {
            claimRepository.saveClaim(newClaim);
        }

        return newClaim;
    }

    /**
     * Indexes a claim into both the player-lookup and spatial-lookup caches.
     *
     * @param claim The claim to cache.
     */
    public void addClaimToCache(@NotNull Claim claim) {
        ClaimRegion region = claim.region();
        String world = region.worldName();

        playerClaims.computeIfAbsent(claim.owner(), k -> ConcurrentHashMap.newKeySet()).add(claim);
        worldClaims.computeIfAbsent(world, k -> ConcurrentHashMap.newKeySet()).add(claim);
        claims.put(claim.claimId(), claim);

        Long2ObjectMap<Set<Claim>> chunkMap = worldChunkMaps.computeIfAbsent(world, k ->
                Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>())
        );

        int minX = region.minX() >> 4;
        int maxX = region.maxX() >> 4;
        int minZ = region.minZ() >> 4;
        int maxZ = region.maxZ() >> 4;

        LongSet chunks = new LongArraySet();

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long key = ChunkUtil.getChunkKey(cx, cz);

                chunks.add(key);
                chunkMap.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                        .add(claim);
            }
        }

        claim.chunks(chunks);
    }

    /**
     * Removes a claim from both the player-lookup and spatial-lookup caches.
     *
     * @param claim The claim to remove from cache.
     */
    public void removeClaimFromCache(@NotNull Claim claim) {
        ClaimRegion region = claim.region();
        String world = region.worldName();

        Set<Claim> userClaims = playerClaims.get(claim.owner());
        if (userClaims != null) {
            userClaims.remove(claim);

            if (userClaims.isEmpty()) {
                playerClaims.remove(claim.owner());
            }
        }

        Set<Claim> wordClaimSet = worldClaims.get(world);
        if (wordClaimSet != null) {
            wordClaimSet.remove(claim);

            if (wordClaimSet.isEmpty()) {
                worldClaims.remove(world);
            }
        }

        claims.remove(claim.claimId());

        Long2ObjectMap<Set<Claim>> chunkMap = worldChunkMaps.get(world);

        if (chunkMap != null) {
            int minX = region.minX() >> 4;
            int maxX = region.maxX() >> 4;
            int minZ = region.minZ() >> 4;
            int maxZ = region.maxZ() >> 4;

            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    long key = ChunkUtil.getChunkKey(cx, cz);

                    Set<Claim> claimsInChunk = chunkMap.get(key);
                    if (claimsInChunk != null) {
                        claimsInChunk.remove(claim);

                        if (claimsInChunk.isEmpty()) {
                            chunkMap.remove(key);
                        }
                    }
                }
            }

            if (chunkMap.isEmpty()) {
                worldChunkMaps.remove(world);
            }
        }

        claim.chunks(new LongArraySet());
    }

    /**
     * Finds all claims covering a specific location.
     *
     * @param loc The location to check.
     * @return A list of claims found at this point; empty list if none.
     */
    @NotNull
    @Contract(pure = true)
    public List<Claim> getClaimsAt(@NotNull Location loc) {
        Map<Long, Set<Claim>> chunkMap = worldChunkMaps.get(loc.getWorld().getName());
        if (chunkMap == null) {
            return List.of();
        }

        long key = ChunkUtil.getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        Set<Claim> claimsInChunk = chunkMap.get(key);

        if (claimsInChunk == null || claimsInChunk.isEmpty()) {
            return List.of();
        }

        return claimsInChunk.stream()
                .filter(claim -> claim.region().contains(loc))
                .toList();
    }

    /**
     * Claims, including sub-claims, whose area overlaps the square of the given radius around the location.
     */
    @NotNull
    @Contract(pure = true)
    public Set<Claim> getClaimsNear(@NotNull Location center, double radius) {
        Map<Long, Set<Claim>> chunkMap = worldChunkMaps.get(center.getWorld().getName());
        if (chunkMap == null) {
            return Set.of();
        }

        int minX = (int) Math.floor(center.getX() - radius);
        int maxX = (int) Math.floor(center.getX() + radius);
        int minZ = (int) Math.floor(center.getZ() - radius);
        int maxZ = (int) Math.floor(center.getZ() + radius);

        Set<Claim> found = new LinkedHashSet<>();

        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                Set<Claim> claimsInChunk = chunkMap.get(ChunkUtil.getChunkKey(chunkX, chunkZ));

                if (claimsInChunk == null) {
                    continue;
                }

                for (Claim claim : claimsInChunk) {
                    ClaimRegion region = claim.region();

                    if (region.minX() <= maxX && region.maxX() >= minX
                            && region.minZ() <= maxZ && region.maxZ() >= minZ) {
                        found.add(claim);
                    }
                }
            }
        }

        return found;
    }

    /**
     * Gets the "most specific" claim at a location.
     * If multiple claims exist (e.g., a sub-claim inside a main claim),
     * the one with the highest depth is returned.
     *
     * @param loc The location to check.
     * @return An Optional containing the deepest claim at the point.
     */
    @NotNull
    @Contract(pure = true)
    public Optional<Claim> getClaimAt(@NotNull Location loc) {
        List<Claim> claims = getClaimsAt(loc);
        if (claims.isEmpty()) {
            return Optional.empty();
        }

        return claims.stream()
                .max(Comparator.comparingInt(this::getClaimDepth));
    }

    /**
     * Whether a change starting at {@code source} may affect {@code target}: allowed when the
     * target isn't claimed, or the source is in a claim with the same owner.
     *
     * @param source The location the change starts from.
     * @param target The location being changed.
     * @return true if the change is allowed.
     */
    public boolean canAffect(@NotNull Location source, @NotNull Location target) {
        Claim targetClaim = getClaimAt(target).orElse(null);
        if (targetClaim == null) {
            return true;
        }

        Claim sourceClaim = getClaimAt(source).orElse(null);

        return sourceClaim != null && sourceClaim.owner().equals(targetClaim.owner());
    }

    /**
     * Gets a claim by its UUID.
     *
     * @param uuid The claim UUID.
     * @return An optional containing the claim with that UUID.
     */
    @NotNull
    @Contract(pure = true)
    public Optional<Claim> getClaim(@NotNull UUID uuid) {
        return Optional.ofNullable(claims.get(uuid));
    }

    /**
     * Obtains the claim depth.
     *
     * @param claim the claim to check
     * @return {@code 0} if it's a main claim, {@code 1} if it's a sub-claim.
     */
    @Contract(pure = true)
    private int getClaimDepth(Claim claim) {
        return (claim.main() == null) ? 0 : 1;
    }

    /**
     * Deletes a claim and removes it from cache
     *
     * @param claim the claim to delete
     */
    public void deleteClaim(@NotNull Claim claim) {
        EventUtil.call(new ClaimDeleteEvent(claim));

        removeClaimFromCache(claim);

        Storage storage = storageHolder.get();

        ClaimRepository claimRepository = storage.claims();
        claimRepository.deleteClaim(claim.claimId());

        if (!claim.subClaims().isEmpty()) {
            List.copyOf(claim.subClaims()).forEach(this::deleteClaim);
        }

        if (claim.main() != null) {
            claim.main().removeSubClaim(claim);
        }
    }

    /**
     * Updates the stored name of a player across every claim they are a member of and
     * persists the ones that changed. Names are stored with the membership, so this is
     * what keeps them current after a player renames.
     *
     * @param uuid The player's id.
     * @param name The player's current name.
     */
    public void refreshMemberName(@NotNull UUID uuid, @NotNull String name) {
        Storage storage = storageHolder.get();

        for (Claim claim : claims.values()) {
            ClaimMember member = claim.getMember(uuid).orElse(null);
            if (member == null || member.lastKnownName().equals(name)) {
                continue;
            }

            member.lastKnownName(name);
            storage.claims().saveMembers(claim);
        }
    }

    /**
     * Gets all the player claims
     *
     * @param uuid the uuid of the owner of the claims
     * @return A {@link List} of {@link Claim}s that are owned by the player
     */
    @NotNull
    @Contract(pure = true)
    public Set<Claim> getPlayerClaims(@NotNull UUID uuid) {
        return Collections.unmodifiableSet(playerClaims.getOrDefault(uuid, Collections.emptySet()));
    }

    /**
     * The claims the player owns, followed by the ones they co-own.
     */
    @NotNull
    public Set<Claim> getManagedClaims(@NotNull UUID uuid) {
        Set<Claim> managed = new LinkedHashSet<>(getPlayerClaims(uuid));

        for (Claim claim : claims.values()) {
            if (claim.getMember(uuid).map(ClaimMember::isCoOwner).orElse(false)) {
                managed.add(claim);
            }
        }

        return managed;
    }

    /**
     * Transfers a player claim and all of its sub claims to a new player, then persists them
     */
    public void transferClaim(@NotNull Claim claimToTransfer, @NotNull UUID oldId, @NotNull User recipient) {
        UUID newId = recipient.uuid();
        Set<Claim> claims = playerClaims.get(oldId);

        if (claims != null && !claims.isEmpty()) {
            Set<Claim> newClaims = playerClaims.computeIfAbsent(newId, k -> ConcurrentHashMap.newKeySet());

            claims.removeIf(claim -> {
                if (claim.claimId().equals(claimToTransfer.claimId()) ||
                        (claim.main() != null && claim.main().claimId().equals(claimToTransfer.claimId()))
                ) {
                    newClaims.add(claim);
                    return true;
                }

                return false;
            });
        }

        Storage storage = storageHolder.get();
        ClaimRepository repository = storage.claims();
        RawClaim snapshot = claimToTransfer.deflate();

        CompletableFuture<Void> userSave = recipient.persistent()
                ? CompletableFuture.completedFuture(null)
                : storage.users().saveUser(recipient);

        if (!recipient.persistent()) {
            userSave.thenRun(() -> recipient.persistent(true));
        }

        repository.saveClaim(snapshot, userSave);

        for (RawClaim subClaim : snapshot.subClaims()) {
            repository.saveClaim(subClaim, userSave);
        }
    }

    /**
     * Checks if a new region overlaps with any existing claim, loaded or not.
     *
     * @param newRegion     The region to test.
     * @param playerId      The player attempting the claim.
     * @param checkForSub   Whether to allow overlap with the player's own main claims (for sub-claiming).
     * @param resizingClaim The claim this player is resizing, {@code null} if it's a new claim.
     * @return {@code true} if an illegal overlap is detected.
     */
    @Contract(pure = true)
    public boolean isOverlapping(@NotNull ClaimRegion newRegion, @NotNull UUID playerId, boolean checkForSub, Claim resizingClaim) {
        int minX = newRegion.minX() >> 4;
        int maxX = newRegion.maxX() >> 4;
        int minZ = newRegion.minZ() >> 4;
        int maxZ = newRegion.maxZ() >> 4;

        String worldName = newRegion.worldName();
        Map<Long, Set<Claim>> worldClaims = worldChunkMaps.get(worldName);

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                long chunkKey = ChunkUtil.getChunkKey(cx, cz);
                Set<Claim> claimsInChunk = worldClaims == null ? null : worldClaims.get(chunkKey);

                if (claimsInChunk != null) {
                    for (Claim existingClaim : claimsInChunk) {
                        if (resizingClaim != null) {
                            if (resizingClaim.claimId().equals(existingClaim.claimId())) {
                                continue;
                            }

                            if (existingClaim.main() != null && resizingClaim.claimId().equals(existingClaim.main().claimId())) {
                                continue;
                            }
                        }

                        if (checkForSub && existingClaim.owner().equals(playerId) && existingClaim.main() == null) {
                            continue;
                        }

                        if (regionsOverlap(newRegion, existingClaim.region())) {
                            return true;
                        }
                    }
                }

                for (RawClaim raw : unloadedClaims.at(worldName, chunkKey)) {
                    if (resizingClaim != null
                            && (raw.claimId().equals(resizingClaim.claimId()) || resizingClaim.claimId().equals(raw.parentId()))) {
                        continue;
                    }

                    if (checkForSub && raw.owner().equals(playerId) && raw.parentId() == null) {
                        continue;
                    }

                    if (regionsOverlap(newRegion, raw)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if two regions are overlapping
     *
     * @param a the first region
     * @param b the second region
     * @return {@code true} if the regions overlap
     */
    private boolean regionsOverlap(@NotNull ClaimRegion a, @NotNull ClaimRegion b) {
        if (!a.worldName().equals(b.worldName())) {
            return false;
        }

        return a.minX() <= b.maxX() && a.maxX() >= b.minX() &&
                a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }

    private boolean regionsOverlap(@NotNull ClaimRegion a, @NotNull RawClaim b) {
        if (!a.worldName().equals(b.worldName())) {
            return false;
        }

        return a.minX() <= b.maxX() && a.maxX() >= b.minX() &&
                a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }
}