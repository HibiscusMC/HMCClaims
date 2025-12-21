package com.hibiscusmc.hmcclaims.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.Inject;

public abstract class AbstractListener implements Listener {

    @Inject
    private Plugin plugin;

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

}