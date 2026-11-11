package com.hibiscusmc.hmcclaims.hook.papi;

import me.clip.placeholderapi.PAPIComponents;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PlaceholderAPI calls that leave the text unchanged when PlaceholderAPI isn't enabled.
 */
public final class Placeholders {

    private Placeholders() {
    }

    private static boolean enabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static @NotNull String apply(@Nullable Player player, @NotNull String text) {
        if (player == null || !enabled()) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static @NotNull Component apply(@Nullable Player player, @NotNull Component component) {
        if (player == null || !enabled()) {
            return component;
        }

        return PAPIComponents.setPlaceholders(player, component);
    }

    public static @NotNull List<Component> apply(@Nullable Player player, @NotNull List<Component> components) {
        if (player == null || !enabled()) {
            return components;
        }

        return PAPIComponents.setPlaceholders(player, components);
    }
}
