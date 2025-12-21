package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.listener.AbstractListener;
import com.hibiscusmc.hmcclaims.listener.PlayerSelectionListener;
import team.unnamed.inject.AbstractModule;

public class ListenerModule extends AbstractModule {

    @Override
    protected void configure() {
        multibind(AbstractListener.class)
                .asSet()
                .to(PlayerSelectionListener.class);
    }

}