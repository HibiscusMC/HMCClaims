package com.hibiscusmc.hmcclaims.storage;

import com.hibiscusmc.hmcclaims.config.Settings;
import lombok.extern.java.Log;
import team.unnamed.inject.Singleton;

@Log(topic = "HMCClaims")
@Singleton
public class StorageHolder {

    private Storage implementation;

    public Storage get() {
        return implementation;
    }

    public void update(Storage implementation) {
        this.implementation = implementation;
    }

    public void initialize(Settings.Storage config) {
        if (implementation == null) {
            throw new IllegalStateException("Storage has not been initialized");
        }

        try {
            implementation.initialize(config);
        } catch (Exception ex) {
            log.severe("Couldn't initialize " + implementation.name() + " storage. Shutting down...");
            throw new RuntimeException(ex);
        }
    }

    public void close() {
        if (implementation == null) {
            throw new IllegalStateException("Storage has not been initialized");
        }

        try {
            implementation.close();
        } catch (Exception ex) {
            log.severe("Couldn't close " + implementation.name() + " storage");
            throw new RuntimeException(ex);
        }
    }

}