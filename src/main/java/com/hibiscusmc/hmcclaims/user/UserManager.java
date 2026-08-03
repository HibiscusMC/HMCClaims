package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages the lifecycle, caching, and block quota calculations for {@link User}s.
 */
@Singleton
public class UserManager {

    // How long an offline user stays cached after their last access
    private final static long TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();
    private final Object2LongMap<UUID> lastAccess = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());

    // De-duplicates concurrent DB loads for the same UUID
    private final Map<UUID, CompletableFuture<User>> loadingUsers = new ConcurrentHashMap<>();

    private final ClaimManager claimManager;

    private final ConfigHolder<Settings> settings;

    private final StorageHolder storageHolder;

    @Inject
    public UserManager(ClaimManager claimManager, ConfigHolder<Settings> settings, StorageHolder storageHolder, SchedulerUtil scheduler) {
        this.claimManager = claimManager;
        this.settings = settings;
        this.storageHolder = storageHolder;

        scheduler.scheduleAsyncTimer(this::cleanupExpiredUsers, TimeUnit.MINUTES.toSeconds(5) * 20L);
    }

    /**
     * Adds a user to the local cache.
     *
     * @param user The {@link User} instance to cache.
     */
    public void cacheUser(@NotNull User user) {
        userMap.put(user.uuid(), user);
    }

    /**
     * Removes a user from the local cache.
     *
     * @param uuid The {@link UUID} of the user to invalidate.
     */
    public void invalidateUser(@NotNull UUID uuid) {
        userMap.remove(uuid);
    }

    /**
     * Retrieves a cached user by their unique identifier.
     *
     * @param uuid The {@link UUID} to look up.
     * @return An {@link Optional} containing the user if found in the cache.
     */
    @NotNull
    @Contract(pure = true)
    public Optional<User> getUser(@NotNull UUID uuid) {
        return Optional.ofNullable(userMap.get(uuid));
    }

    /**
     * Forces a recalculation of used blocks for a specific UUID.
     *
     * @param uuid The identifier of the user to update.
     * @throws IllegalStateException if the user is not currently cached.
     */
    public void calculateUsedBlocks(@NotNull UUID uuid) {
        User user = getUser(uuid).orElseThrow(() -> new IllegalStateException("User " + uuid + " has not been loaded yet"));

        calculateUsedBlocks(user);
    }

    /**
     * Recalculates and updates the cached 'usedBlocks' value within the {@link User} object.
     * <p>
     * This scans all top-level claims (excluding sub-claims) owned by the user
     * and sums their surface area.
     *
     * @param user The user object to update.
     */
    public void calculateUsedBlocks(@NotNull User user) {
        user.usedBlocks(claimManager.getPlayerClaims(user.uuid()).stream()
                .filter(claim -> claim.main() == null)
                .mapToLong(claim -> claim.region().getSurfaceArea())
                .sum());
    }

    /**
     * Returns the number of blocks a user has left to spend.
     * <p>
     * If the user's used blocks haven't been calculated yet, this method will
     * trigger an initial calculation.
     *
     * @param user The user to check.
     * @return The available block balance. Returns {@code 0} if the user is null.
     */
    public long getRemainingBlocks(@Nullable User user) {
        if (user == null) {
            return 0;
        }

        // Lazy-loading the block calculation if it's missing
        if (user.usedBlocks() == null) {
            calculateUsedBlocks(user);
        }

        long starting = settings.get().claimBlocks().startingAmount();
        return (starting + user.claimBlocks()) - user.usedBlocks();
    }

    /**
     * Returns the cached user if present, otherwise loads them from storage,
     * caching the result. Concurrent calls for the same UUID share a single load.
     */
    @NotNull
    public CompletableFuture<@Nullable User> getOrLoadUser(@NotNull UUID uuid) {
        User cached = userMap.get(uuid);
        if (cached != null) {
            touch(uuid);
            return CompletableFuture.completedFuture(cached);
        }

        return loadingUsers.computeIfAbsent(uuid, id -> {
            Storage storage = storageHolder.get();

            return storage.users().getUser(id).thenApply(user -> {
                if (user != null) {
                    cacheUser(user);
                }

                return user;
            }).whenComplete((user, throwable) -> loadingUsers.remove(id));
        });
    }

    private void touch(UUID uuid) {
        lastAccess.put(uuid, System.currentTimeMillis());
    }

    /**
     * Evicts offline, expired users from cache, saving them first.
     * Should be scheduled periodically (e.g. every minute).
     */
    public void cleanupExpiredUsers() {
        long now = System.currentTimeMillis();
        Storage storage = storageHolder.get();

        Iterator<Map.Entry<UUID, User>> it = userMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, User> entry = it.next();
            UUID uuid = entry.getKey();

            if (Bukkit.getPlayer(uuid) != null) {
                continue;
            }

            if (!lastAccess.containsKey(uuid)) {
                continue;
            }

            long last = lastAccess.getLong(uuid);
            if (now - last < TTL_MILLIS) {
                continue;
            }

            it.remove();
            lastAccess.removeLong(uuid);
            storage.users().saveUser(entry.getValue());
        }
    }
}