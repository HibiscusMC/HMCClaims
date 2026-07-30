package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.listener.ClaimLifecycleListener;
import com.hibiscusmc.hmcclaims.listener.IntegrationListener;
import com.hibiscusmc.hmcclaims.listener.PlayerDataListener;
import com.hibiscusmc.hmcclaims.listener.PlayerInputListener;
import com.hibiscusmc.hmcclaims.listener.PlayerSelectionListener;
import com.hibiscusmc.hmcclaims.listener.permission.VanillaBlockListener;
import org.bukkit.event.Listener;
import team.unnamed.inject.AbstractModule;

public class ListenerModule extends AbstractModule {

    @Override
    protected void configure() {
        multibind(Listener.class)
                .asSet()
                .to(PlayerSelectionListener.class)
                .to(PlayerDataListener.class)
                .to(IntegrationListener.class)
                .to(PlayerInputListener.class)
                .to(ClaimLifecycleListener.class)

                /* Permission Listeners */
                .to(VanillaBlockListener.class);
    }
}