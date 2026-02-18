package com.hibiscusmc.hmcclaims.service;

import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.impl.remote.MariaDBStorage;
import lombok.extern.java.Log;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

@Log(topic = "HMCClaims")
public class StorageService implements Service {

    @Inject
    private ConfigHolder<Settings> settings;
    @Inject
    private StorageHolder holder;

    @Inject
    private Injector injector;

    @Override
    public void start() {
        Settings.Storage storage = settings.get().storage();

        Storage impl;
        switch (storage.method()) {
            case MARIADB -> impl = injector.getInstance(MariaDBStorage.class);
            default -> {
                if (!storage.method().name().equalsIgnoreCase("H2")) {
                    log.warning("Unsupported storage method: " + storage.method());
                    log.warning("Defaulting to H2");
                }

                // TODO: implement flatfile
                impl = null;
            }
        }

        holder.update(impl);
        log.info("Using " + storage.method().methodName() + " for storage.");

        holder.initialize(storage);
    }

    @Override
    public void reload() {

    }

    @Override
    public void stop() {
        if (holder.get() == null) {
            return;
        }

        holder.close();
    }
}