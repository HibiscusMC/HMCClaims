package com.hibiscusmc.hmcclaims.service;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.storage.impl.local.H2Storage;
import com.hibiscusmc.hmcclaims.storage.impl.remote.MariaDBStorage;
import com.hibiscusmc.hmcclaims.util.ExceptionUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import org.bukkit.Bukkit;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

import java.util.concurrent.TimeUnit;

public class StorageService implements Service {

    private static final int STARTUP_ATTEMPTS = 3;
    private static final long RETRY_DELAY_SECONDS = 3;

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
            case H2 -> impl = injector.getInstance(H2Storage.class);
            case MARIADB -> impl = injector.getInstance(MariaDBStorage.class);
            default -> throw new IllegalArgumentException("Unsupported storage method: " + storage.method());
        }

        String methodName = storage.method().methodName();

        holder.update(impl);
        Logger.log("Using " + methodName + " for storage.");

        for (int attempt = 1; attempt <= STARTUP_ATTEMPTS; attempt++) {
            try {
                holder.initialize(storage);

                return;
            } catch (RuntimeException ex) {
                try {
                    holder.close();
                } catch (RuntimeException ignored) {
                }

                if (attempt == STARTUP_ATTEMPTS) {
                    throw failStartup(methodName, ex);
                }

                Logger.warning("Couldn't connect to the {} storage (attempt {}/{}): {}. Retrying in {}s...",
                        methodName, attempt, STARTUP_ATTEMPTS, ExceptionUtil.rootMessage(ex), RETRY_DELAY_SECONDS);

                try {
                    TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();

                    throw failStartup(methodName, ex);
                }
            }
        }
    }

    private RuntimeException failStartup(String methodName, RuntimeException ex) {
        Logger.error("Couldn't connect to the " + methodName + " storage after " + STARTUP_ATTEMPTS
                + " attempts. Shutting down the server so claims aren't left unprotected.", ex);
        Bukkit.shutdown();

        return ex;
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