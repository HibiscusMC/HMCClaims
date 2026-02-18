package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Settings;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class UserManager {

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    @Inject
    private ClaimManager claimManager;

    @Inject
    private ConfigHolder<Settings> settings;

    public void cacheUser(User user) {
        userMap.put(user.uuid(), user);
    }

    public void invalidateUser(UUID uuid) {
        userMap.remove(uuid);
    }

    public Optional<User> getUser(UUID uuid) {
        return Optional.ofNullable(userMap.get(uuid));
    }

    public long getRemainingBlocks(User user) {
        if (user == null) {
            return 0;
        }

        long usedBlocks = claimManager.getPlayerClaims(user.uuid()).stream()
                .filter(claim -> claim.parent() == null)
                .mapToLong(claim -> claim.region().getSurfaceArea())
                .sum();

        return (settings.get().claimBlocks().startingAmount() + user.claimBlocks()) - usedBlocks;
    }

}