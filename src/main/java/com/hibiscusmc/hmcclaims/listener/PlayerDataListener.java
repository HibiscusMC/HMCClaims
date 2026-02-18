package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import team.unnamed.inject.Inject;

import java.time.Instant;
import java.util.UUID;

public class PlayerDataListener implements Listener {

    @Inject
    private UserManager manager;

    @Inject
    private StorageHolder holder;

    @Inject
    private Text text;

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        UUID uuid = event.getUniqueId();

        holder.get().users().getUser(uuid)
                .thenAccept(user -> {
                    if (user == null) {
                        user = new User(uuid, playerName);
                    }

                    if (!user.lastKnownName().equals(playerName)) {
                        user.lastKnownName(playerName);
                    }

                    user.lastOnline(Instant.now());

                    manager.cacheUser(user);
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    return null;
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        manager.getUser(uuid).ifPresent(user ->
                holder.get().users().saveUser(user).whenComplete((value, ex) -> {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }

                    manager.invalidateUser(uuid);
                })
        );
    }
}