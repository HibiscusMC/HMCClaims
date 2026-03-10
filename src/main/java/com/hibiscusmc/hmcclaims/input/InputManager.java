package com.hibiscusmc.hmcclaims.input;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.task.InputDisplayTask;
import net.kyori.adventure.audience.Audience;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for managing input lifecycles
 */
@Singleton
public class InputManager {

    private final static Map<Audience, Input<?>> PLAYER_INPUTS
            = new HashMap<>();

    private final InputRegistry registry;

    private InputDisplayTask task;

    @Inject
    private Plugin plugin;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    public InputManager() {
        this.registry = new InputRegistry();
    }

    /**
     * Tries to fetch the current input a player is on.
     *
     * @param audience The {@link Audience} instance of the player
     * @param <T>      The type of the {@link Input}
     * @return The {@link Input}
     * @throws ClassCastException If the return type doesn't match the actual type of the input
     */
    @Nullable
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public <T> Input<T> fetch(@NotNull Audience audience) {
        return (Input<T>) PLAYER_INPUTS.get(audience);
    }

    /**
     * Creates a new {@link Input} instance using the provided type
     *
     * @param audience The {@link Audience} instance of the player
     * @param type     The type of {@link Input} to create
     * @param <T>      Tye type of the {@link Input}
     * @return The newly created {@link Input}, or {@code null} if the player is already in an input or the input type doesn't exist
     */
    @Nullable
    public <T> Input<T> create(@NotNull Audience audience, @NotNull Class<T> type) {
        if (PLAYER_INPUTS.containsKey(audience)) {
            return null;
        }

        Input<T> input = registry.get(type);
        if (input == null) {
            return null;
        }

        task = InputDisplayTask.start(plugin, this, messagesHolder);
        PLAYER_INPUTS.put(audience, input);
        return input;
    }

    /**
     * Destroys the {@link Input} this player is on
     *
     * @param audience The {@link Audience} instance of the player
     */
    public <T> void destroy(@NotNull Audience audience) {
        Input<T> input = fetch(audience);
        if (input == null) {
            return;
        }

        if (task != null) {
            task.clear(audience);
        }

        PLAYER_INPUTS.remove(audience);

        if (PLAYER_INPUTS.isEmpty() && task != null) {
            cancelTask();
        }
    }

    public Audience audiences() {
        return Audience.audience(PLAYER_INPUTS.keySet());
    }

    /**
     * Cancels the current task and removes the reference to it.
     */
    public void cancelTask() {
        task.cancel();
        task = null;
    }
}