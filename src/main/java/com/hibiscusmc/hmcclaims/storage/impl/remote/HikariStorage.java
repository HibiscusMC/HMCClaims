package com.hibiscusmc.hmcclaims.storage.impl.remote;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.storage.repository.UserRepository;
import com.hibiscusmc.hmcclaims.storage.repository.sql.SQLClaimRepository;
import com.hibiscusmc.hmcclaims.storage.repository.sql.SQLUserRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class HikariStorage implements Storage {

    private final ExecutorService executor = Executors.newFixedThreadPool(10,
            new ThreadFactoryBuilder()
                    .setNameFormat("hmcclaims-hikari-%d")
                    .setDaemon(true)
                    .build()
    );

    private HikariDataSource dataSource;

    @Setter
    private UserRepository users;
    @Setter
    private ClaimRepository claims;

    protected abstract String dataSourceClassName();

    protected abstract void setup(Settings.Storage storage);

    protected void setProperties(Properties properties) {
    }

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

        this.setup(storage);

        users = new SQLUserRepository(storage, this, executor);
        claims = new SQLClaimRepository(storage, this, executor);
    }

    @Override
    public void close() {
        if (dataSource == null) {
            return;
        }

        dataSource.close();
    }

    @Override
    public UserRepository users() {
        return users;
    }

    @Override
    public ClaimRepository claims() {
        return claims;
    }

    protected InputStream getSchemaResource(String database) {
        return getClass().getClassLoader().getResourceAsStream("schemas/" + database + ".sql");
    }
}