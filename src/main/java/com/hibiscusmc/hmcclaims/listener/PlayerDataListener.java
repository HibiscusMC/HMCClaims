package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import team.unnamed.inject.Inject;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the loading and saving of {@link User} data during player connection events.
 */
public class PlayerDataListener implements Listener {

    @Inject
    private UserManager manager;

    @Inject
    private StorageHolder holder;

    /**
     * Loads player data from storage before the player joins.
     * <p>
     * <b>Note:</b> This implementation uses {@code join()} to block the pre-login
     * thread. This is intentional to ensure data is fully loaded before the player
     * is allowed into the world, preventing race conditions in other managers.
     *
     * @param event The pre-login event.
     */
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        UUID uuid = event.getUniqueId();

        Storage storage;
        try {
            storage = holder.get();
        } catch (IllegalStateException e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    TextUtil.parse("<red>The claims storage was not initialized properly."));
            return;
        }

        try {
            storage.users().getUser(uuid).thenAccept(user -> {
                if (user == null) {
                    user = new User(uuid, playerName, 0L);
                }

                if (!user.lastKnownName().equals(playerName)) {
                    user.lastKnownName(playerName);
                }

                user.lastOnline(Instant.now());
                manager.cacheUser(user);
            }).join();
        } catch (Exception ex) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    TextUtil.parse("<red>Failed to load your player data. Please try again later."));

            Logger.error("Couldn't load player data.", ex);
        }
    }

    /**
     * Saves player data to storage and invalidates the cache upon disconnection.
     *
     * @param event The quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Storage storage = holder.get();

        manager.getUser(uuid).ifPresent(user ->
                storage.users().saveUser(user).whenComplete((value, ex) -> {
                    if (ex != null) {
                        Logger.error("Couldn't save player data.", ex);

                        return;
                    }

                    manager.invalidateUser(uuid);
                })
        );
    }
}