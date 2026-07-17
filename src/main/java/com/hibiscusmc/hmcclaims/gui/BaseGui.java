package com.hibiscusmc.hmcclaims.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The core interface for all GUIs within the plugin.
 */
public interface BaseGui {

    /**
     * Instructs the GUI to (re)load its visual components, such as item icons,
     * titles, and layout from the configuration files.
     */
    void loadConfig();

    /**
     * Opens the standard version of this interface for the specified player.
     *
     * @param player The player who will view the GUI.
     */
    default void open(@NotNull Player player) {
    }

    /**
     * Opens a context-specific version of this interface for the player.
     *
     * @param player The player who will view the GUI.
     * @param args   Optional contextual arguments (e.g., Claim objects, role data).
     */
    default void open(@NotNull Player player, Object... args) {
    }

    /**
     * Truncates the claim name to the title's maximum length, appending "..." if exceeded.
     *
     * @param claimName the original claim name to parse
     * @param maxLength the maximum allowed length for the claim name
     * @return the potentially truncated claim name, or the original if within limits
     */
    default String parseName(@NotNull String claimName, int maxLength) {
        if (maxLength < 0 || claimName.length() <= maxLength) {
            return claimName;
        }

        return claimName.substring(0, maxLength) + "...";
    }
}