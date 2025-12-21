package com.hibiscusmc.hmcclaims.storage;

import com.hibiscusmc.hmcclaims.config.Settings;

public interface Storage {

    String name();

    void initialize(Settings.Storage storage);

    void close();

    void logTables();

}