package com.hibiscusmc.hmcclaims.input;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.task.InputDisplayTask;
import com.hibiscusmc.hmcclaims.user.PlayerResolver;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for managing input lifecycles
 */
@Singleton
public class InputManager {

    private final Map<UUID, Input<?>> inputs
            = new ConcurrentHashMap<>();

    private final Set<UUID> submitting
            = ConcurrentHashMap.newKeySet();

    private final InputRegistry registry;

    private InputDisplayTask task;

    @Inject
    private Plugin plugin;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    public InputManager(PlayerResolver resolver) {
        this.registry = new InputRegistry(resolver);
    }

    /**
     * Tries to fetch the current input a player is on.
     *
     * @param player The player
     * @param <T>    The type of the {@link Input}
     * @return The {@link Input}
     * @throws ClassCastException If the return type doesn't match the actual type of the input
     */
    @Nullable
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public <T> Input<T> fetch(@NotNull Player player) {
        return (Input<T>) inputs.get(player.getUniqueId());
    }

    /**
     * Creates a new {@link Input} instance using the provided type
     *
     * @param player The player
     * @param type   The type of {@link Input} to create
     * @param <T>    Tye type of the {@link Input}
     * @return The newly created {@link Input}, or {@code null} if the player is already in an input or the input type doesn't exist
     */
    @Nullable
    public <T> Input<T> create(@NotNull Player player, @NotNull Class<T> type) {
        Input<T> input = registry.create(type);

        if (input == null) {
            return null;
        }

        if (inputs.putIfAbsent(player.getUniqueId(), input) != null) {
            return null;
        }

        if (task == null) {
            task = InputDisplayTask.start(plugin, this, messagesHolder);
        }

        return input;
    }

    /**
     * Destroys the {@link Input} this player is on
     *
     * @param player The player
     */
    public void destroy(@NotNull Player player) {
        UUID id = player.getUniqueId();
        submitting.remove(id);

        if (inputs.remove(id) == null) {
            return;
        }

        if (task != null) {
            task.clear(player);
        }

        if (inputs.isEmpty() && task != null) {
            cancelTask();
        }
    }

    /**
     * Returns false if a submission for this player is already being processed.
     */
    public boolean beginSubmit(@NotNull Player player) {
        return submitting.add(player.getUniqueId());
    }

    public void endSubmit(@NotNull Player player) {
        submitting.remove(player.getUniqueId());
    }

    public Audience audiences() {
        return Audience.audience(inputs.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList());
    }

    /**
     * Cancels the current task and removes the reference to it.
     */
    public void cancelTask() {
        if (task == null) {
            return;
        }

        task.cancel();
        task = null;
    }
}
