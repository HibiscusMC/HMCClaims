package com.hibiscusmc.hmcclaims.service;

import com.hibiscusmc.hmcclaims.listener.AbstractListener;
import team.unnamed.inject.Inject;

import java.util.Set;

public class ListenerService implements Service {

    @Inject
    private Set<AbstractListener> listeners;

    @Override
    public void start() {
        for (AbstractListener listener : listeners) {
            listener.register();
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public void stop() {
    }

}