package com.hibiscusmc.hmcclaims.storage.impl.remote;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.impl.HikariStorage;
import org.jetbrains.annotations.NotNull;

public class MariaDBStorage extends HikariStorage {

    @Override
    public @NotNull String name() {
        return "MariaDB";
    }

    @Override
    protected String dataSourceClassName() {
        return "org.mariadb.jdbc.MariaDbDataSource";
    }

    @Override
    public void setup(@NotNull Settings.Storage storage) {
        executeSchemaScript(storage, name().toLowerCase());
    }
}