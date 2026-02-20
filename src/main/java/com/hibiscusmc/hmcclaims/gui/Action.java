package com.hibiscusmc.hmcclaims.gui;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public abstract class Action {

    private final String rawAction;

    public Action(String rawAction) {
        this.rawAction = rawAction;
    }

    public abstract void execute(Player player);

    public static Action parse(String input) {
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