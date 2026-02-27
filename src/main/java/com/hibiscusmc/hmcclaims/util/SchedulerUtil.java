package com.hibiscusmc.hmcclaims.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

/**
 * Utility for managing task scheduling and thread switching within the plugin.
 * <p>
 * This class abstracts the Bukkit Scheduler to provide a simplified interface
 * for running synchronous and asynchronous tasks.
 */
@Singleton
public class SchedulerUtil {

    @Inject
    private Plugin plugin;

    /**
     * Executes a task asynchronously.
     *
     * @param runnable The task to execute.
     */
    public void scheduleAsync(@NotNull Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Executes a task on the main server thread during the next tick.
     *
     * @param runnable The task to execute.
     */
    public void schedule(@NotNull Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    /**
     * Schedules a repeated task to run on the main thread.
     *
     * @param runnable The task to execute repeatedly.
     * @param interval The interval in server ticks.
     */
    public void scheduleTimer(@NotNull Runnable runnable, long interval) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, 0L, interval);
    }

    /**
     * Schedules a repeated task to run asynchronously.
     *
     * @param runnable The task to execute repeatedly.
     * @param interval The interval in server ticks.
     */
    public void scheduleAsyncTimer(@NotNull Runnable runnable, long interval) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, interval);
    }
}