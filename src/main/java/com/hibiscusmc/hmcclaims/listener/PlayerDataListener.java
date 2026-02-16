package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import team.unnamed.inject.Inject;

import java.time.Instant;

public class PlayerDataListener implements Listener {

    @Inject
    private UserManager manager;

    @Inject
    private StorageHolder holder;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        holder.get().users().getUser(player.getUniqueId())
                .thenAccept(user -> {
                    if (user == null) {
                        throw new NullPointerException("User is not on database");
                    }

                    String playerName = player.getName();
                    if (!user.lastKnownName().equals(player.getName())) {
                        user.lastKnownName(playerName);
                    }

                    user.lastOnline(Instant.now());

                    manager.cacheUser(user);
                })
                .exceptionally(ex -> {
                    User user = new User(
                            player.getUniqueId(),
                            player.getName()
                    );
                    user.lastOnline(Instant.now());

                    manager.cacheUser(user);

                    return null;
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        manager.getUser(player.getUniqueId()).ifPresent(user -> holder.get().users().saveUser(user));
    }
}