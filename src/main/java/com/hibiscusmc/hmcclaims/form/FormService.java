package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.spec.FormSpec;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

/**
 * Entry point for everything Bedrock-facing: platform detection, form delivery, and the
 * per-player navigation history that backs the Back buttons.
 */
@Singleton
public class FormService {

    /**
     * Hard cap on the navigation history so a player looping through menus can't grow it
     * without bound.
     */
    private final static int MAX_HISTORY = 32;

    /**
     * Navigation history per player. Concurrent because lists are assembled off the main
     * thread, the same way the inventory GUIs build themselves.
     */
    private final Map<UUID, Deque<Runnable>> history = new ConcurrentHashMap<>();

    /**
     * Per-player scratch state that has to survive a form being re-sent, such as the
     * active search query of a list. Forms cannot be updated in place, so anything the
     * player has adjusted on screen lives here rather than in the form itself.
     */
    private final Map<UUID, Map<String, Object>> state = new ConcurrentHashMap<>();

    @Inject
    private ConfigHolder<Settings> settingsHolder;

    @Inject
    private SchedulerUtil scheduler;

    @Nullable
    private FloodgateBridge bridge;

    /**
     * Activates Bedrock support. Called by the Floodgate hook.
     */
    public void enable() {
        bridge = new FloodgateBridge(scheduler);
    }

    /**
     * Deactivates Bedrock support and drops every stored navigation history.
     */
    public void disable() {
        bridge = null;
        history.clear();
        state.clear();
    }

    /**
     * @return {@code true} if Floodgate is present and forms are enabled in the config.
     */
    public boolean available() {
        Settings settings = settingsHolder.get();

        return bridge != null && settings != null && settings.forms().enabled();
    }

    /**
     * @return {@code true} if buttons without a configured image may borrow the Bedrock
     * texture of the matching GUI icon's material.
     */
    public boolean materialImageFallback() {
        Settings settings = settingsHolder.get();

        return settings != null && settings.forms().materialImageFallback();
    }

    /**
     * Checks whether this player is connected through Geyser.
     *
     * @param player The player to test.
     * @return {@code true} for Bedrock players while form support is active.
     */
    public boolean isBedrock(@NotNull Player player) {
        return available() && bridge.isBedrock(player.getUniqueId());
    }

    /**
     * Renders and delivers a form.
     *
     * @param player The recipient.
     * @param spec   The form to show.
     * @return {@code true} if the form was delivered.
     */
    public boolean send(@NotNull Player player, @NotNull FormSpec spec) {
        if (!available()) {
            return false;
        }

        player.closeInventory();

        return bridge.send(player.getUniqueId(), spec);
    }

    /**
     * Starts a fresh navigation history and shows the given screen. Use this for entry
     * points such as commands, where there is nothing sensible to go back to.
     *
     * @param player The recipient.
     * @param open   Opens the root screen.
     */
    public void root(@NotNull Player player, @NotNull Runnable open) {
        clear(player);

        open.run();
    }

    /**
     * Records how to return to the screen the player is currently on.
     *
     * @param player The player navigating.
     * @param reopen Re-renders the screen being left.
     */
    public void push(@NotNull Player player, @NotNull Runnable reopen) {
        Deque<Runnable> stack = history.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentLinkedDeque<>());

        if (stack.size() >= MAX_HISTORY) {
            stack.removeLast();
        }

        stack.push(reopen);
    }

    /**
     * Returns to the previous screen.
     *
     * @param player The player navigating.
     * @return {@code false} if there was nothing to go back to.
     */
    public boolean back(@NotNull Player player) {
        Deque<Runnable> stack = history.get(player.getUniqueId());

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        stack.pop().run();
        return true;
    }

    /**
     * @param player The player to test.
     * @return {@code true} if there is a screen to go back to.
     */
    public boolean hasHistory(@NotNull Player player) {
        Deque<Runnable> stack = history.get(player.getUniqueId());

        return stack != null && !stack.isEmpty();
    }

    /**
     * Reads a player's scratch state, creating it on first use.
     *
     * @param player  The player the state belongs to.
     * @param key     Identifies the form the state belongs to.
     * @param factory Creates the initial state.
     * @param <T>     The state type.
     * @return The stored state.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T state(@NotNull Player player, @NotNull String key, @NotNull Supplier<T> factory) {
        return (T) state.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, id -> factory.get());
    }

    /**
     * Forgets a player's navigation history and scratch state.
     *
     * @param player The player to clear.
     */
    public void clear(@NotNull Player player) {
        clear(player.getUniqueId());
    }

    /**
     * Forgets a player's navigation history and scratch state. Used on quit, where no
     * {@link Player} instance should be retained.
     *
     * @param uuid The player's unique id.
     */
    public void clear(@NotNull UUID uuid) {
        history.remove(uuid);
        state.remove(uuid);
    }
}