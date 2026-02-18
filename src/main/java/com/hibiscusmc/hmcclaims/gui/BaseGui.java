package com.hibiscusmc.hmcclaims.gui;

import org.bukkit.entity.Player;

public interface BaseGui {

    void loadConfig();

    default void open(Player player) {
    }

    default void open(Player player, Object... args) {
    }

}