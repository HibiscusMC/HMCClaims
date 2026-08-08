package com.hibiscusmc.hmcclaims.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Singleton;

/**
 * Utility class for dispatching events.
 */
@Singleton
public class EventUtil {

    /**
     * Calls a Bukkit event.
     *
     * @param event the event to dispatch
     */
    public static void call(@NotNull Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}