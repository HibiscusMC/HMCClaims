package com.hibiscusmc.hmcclaims.gui;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a functional task that can be executed by or on behalf of a player.
 */
@Getter
public abstract class Action {

    /**
     * The original string representation of the action.
     */
    private final String rawAction;

    public Action(String rawAction) {
        this.rawAction = rawAction;
    }

    /**
     * Performs the logic associated with this action for the given player.
     *
     * @param player The player context for placeholders and execution.
     */
    public abstract void execute(Player player);

    /**
     * Parses a configuration string into a concrete Action instance.
     * <p>
     * <strong>Supported formats:</strong>
     * <ul>
     * <li>{@code CONSOLE: command} - Executes a command as the console.</li>
     * <li>{@code COMMAND: command} - Makes the player perform a command.</li>
     * <li>{@code MESSAGE: text} - Sends a MiniMessage-formatted message to the player.</li>
     * </ul>
     *
     * @param input The raw action string from config.
     * @return A concrete implementation of {@link Action}.
     * @throws IllegalArgumentException if the syntax is invalid or the type is unknown.
     */
    @NotNull
    public static Action parse(@NotNull String input) {
        String[] split = input.split(":");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid action syntax");
        }

        String action = split[0].toUpperCase();
        String value = Arrays.stream(split)
                .skip(1)
                .collect(Collectors.joining(":"))
                .trim();

        return switch (action) {
            case "CONSOLE" -> new Action(input) {
                @Override
                public void execute(Player player) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, value));
                }
            };

            case "COMMAND" -> new Action(input) {
                @Override
                public void execute(Player player) {
                    player.performCommand(PlaceholderAPI.setPlaceholders(player, value));
                }
            };

            case "MESSAGE" -> new Action(input) {
                @Override
                public void execute(Player player) {
                    player.sendRichMessage(PlaceholderAPI.setPlaceholders(player, value));
                }
            };
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        };
    }
}