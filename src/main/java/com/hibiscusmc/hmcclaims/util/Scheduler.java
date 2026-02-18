package com.hibiscusmc.hmcclaims.util;

import org.bukkit.plugin.Plugin;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

@Singleton
public class Scheduler {

    @Inject
    private Plugin plugin;

    public void scheduleAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public void schedule(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public void scheduleTimer(Runnable runnable, long delay) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, delay);
    }
}