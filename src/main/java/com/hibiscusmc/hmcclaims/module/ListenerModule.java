package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.listener.PlayerSelectionListener;
import org.bukkit.event.Listener;
import team.unnamed.inject.AbstractModule;

public class ListenerModule extends AbstractModule {
    @Override
    protected void configure() {
        multibind(Listener.class)
                .asSet()
                .to(PlayerSelectionListener.class);
    }
}