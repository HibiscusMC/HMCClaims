package com.hibiscusmc.hmcclaims.storage.impl.remote;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.SQLStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class HikariStorage extends SQLStorage {

    private HikariDataSource dataSource;

    protected abstract String dataSourceClassName();

    protected abstract void setup();

    protected void setProperties(Properties properties) {
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized.");
        }

        Connection connection = dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Connection not initialized.");
        }

        return connection;
    }

    @Override
    public void initialize(Settings.Storage storage) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("hmcclaims-storage");

        Settings.Storage.Remote remote = storage.remote();
        config.setDataSourceClassName(dataSourceClassName());

        config.addDataSourceProperty("url", remote.uri().isEmpty() ? String.format(
                "jdbc:mariadb://%s:%d/%s",
                remote.address(),
                remote.port(),
                storage.database()
        ) : remote.uri());
        config.addDataSourceProperty("user", remote.username());
        config.addDataSourceProperty("password", remote.password());

        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        config.setMaximumPoolSize(10);
        config.setKeepaliveTime(0);
        config.setMinimumIdle(10);

        this.setProperties(config.getDataSourceProperties());

        this.dataSource = new HikariDataSource(config);

        this.setup();
    }

    @Override
    public void close() {
        if (dataSource == null) {
            return;
        }

        dataSource.close();
    }
}