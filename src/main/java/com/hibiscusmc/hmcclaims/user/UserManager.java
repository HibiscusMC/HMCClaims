package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle, caching, and block quota calculations for {@link User}s.
 */
@Singleton
public class UserManager {

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Settings> settings;

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
}