package com.hibiscusmc.hmcclaims.service;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.impl.remote.MariaDBStorage;
import com.hibiscusmc.hmcclaims.util.Logger;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

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
            default -> throw new IllegalArgumentException("Unsupported storage method: " + storage.method());
        }

        holder.update(impl);
        Logger.log("Using " + storage.method().methodName() + " for storage.");

        holder.initialize(storage);
    }

    @Override
    public void reload() {

    }

    @Override
    public void stop() {
        try {
            // noinspection ResultOfMethodCallIgnored
            holder.get();

            holder.close();
        } catch (IllegalStateException ignored) {
        }
    }
}