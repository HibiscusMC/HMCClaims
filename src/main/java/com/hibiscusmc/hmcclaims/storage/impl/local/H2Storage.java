package com.hibiscusmc.hmcclaims.storage.impl.local;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.impl.HikariStorage;
import com.zaxxer.hikari.HikariConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

public class H2Storage extends HikariStorage {

    @Inject
    private Plugin plugin;

    @Override
    public @NotNull String name() {
        return "H2";
    }

    @Override
    protected String dataSourceClassName() {
        return "org.h2.jdbcx.JdbcDataSource";
    }

    @Override
    protected void configureConnection(@NotNull HikariConfig config, @NotNull Settings.Storage storage) {
        String url = "jdbc:h2:" + getStoragePath(storage.database())
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";

        config.addDataSourceProperty("URL", url);
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "");
    }

    @Override
    public void setup(@NotNull Settings.Storage storage) {
        executeSchemaScript(storage, name().toLowerCase());
    }

    @NotNull
    private String getStoragePath(@NotNull String databaseName) {
        return plugin.getDataPath().resolve(databaseName + "-h2").toFile().getAbsolutePath();
    }
}