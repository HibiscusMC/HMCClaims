package com.hibiscusmc.hmcclaims.service;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import team.unnamed.inject.Inject;

import java.util.Set;

public class ListenerService implements Service {

    @Inject
    private Set<Listener> listeners;

    @Inject
    private Plugin plugin;

    @Override
    public void start() {
        PluginManager manager = plugin.getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, plugin);
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public void stop() {
    }
}