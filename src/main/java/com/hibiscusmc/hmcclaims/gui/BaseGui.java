package com.hibiscusmc.hmcclaims.gui;

import org.bukkit.entity.Player;

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
    default void open(Player player) {
    }

    /**
     * Opens a context-specific version of this interface for the player.
     *
     * @param player The player who will view the GUI.
     * @param args   Optional contextual arguments (e.g., Claim objects, role data).
     */
    default void open(Player player, Object... args) {
    }

}