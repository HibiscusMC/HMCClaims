package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

public class PlayerInputListener implements Listener {

    private final static PlainTextComponentSerializer PLAIN_TEXT
            = PlainTextComponentSerializer.plainText();

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private InputManager manager;

    @Inject
    private SchedulerUtil scheduler;

    @Inject
    private TextUtil text;

    @EventHandler
    public void onPlayerInput(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Input<?> input = manager.fetch(player);

        if (input == null) {
            return;
        }

        event.setCancelled(true);

        if (!manager.beginSubmit(player)) {
            return;
        }

        String raw = PLAIN_TEXT.serialize(event.message());
        Messages messages = messagesHolder.get();

        scheduler.schedule(() -> {
            if (manager.fetch(player) != input) {
                manager.endSubmit(player);
                return;
            }

            input.submit(raw).whenComplete((error, throwable) -> {
                manager.endSubmit(player);

                if (throwable != null) {
                    Logger.error("Couldn't process chat input.", throwable);
                    return;
                }

                if (error == null) {
                    manager.destroy(player);
                    return;
                }

                switch (error) {
                    case PLAYER_NOT_FOUND -> text.send(player, messages.commands().playerNotFound());
                }
            });
        });
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Input<?> input = manager.fetch(player);

        if (input == null) {
            return;
        }

        if (!event.isSneaking()) {
            return;
        }

        scheduler.schedule(() -> {
            input.cancel();
            manager.destroy(player);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.destroy(event.getPlayer());
    }
}
