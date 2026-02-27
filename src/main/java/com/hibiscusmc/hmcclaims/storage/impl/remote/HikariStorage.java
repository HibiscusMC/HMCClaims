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
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An abstract SQL-based storage implementation using the HikariCP connection pool.
 */
public abstract class HikariStorage implements Storage {

    /**
     * Dedicated thread pool for executing database operations off the main server thread.
     */
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

    /**
     * Returns the driver-specific DataSource class name.
     *
     * @return The fully qualified class name (e.g., "org.mariadb.jdbc.MariaDbDataSource").
     */
    protected abstract String dataSourceClassName();

    /**
     * Performs implementation-specific table setup and migrations.
     * <p>
     * This is called at the end of {@link #initialize(Settings.Storage)} after
     * the data source has been established.
     *
     * @param storage The storage configuration.
     */
    protected abstract void setup(@NotNull Settings.Storage storage);

    /**
     * Allows subclasses to inject additional driver-specific properties into the pool.
     *
     * @param properties The property set to modify.
     */
    protected void setProperties(@NotNull Properties properties) {
    }

    /**
     * Borrows a connection from the Hikari connection pool.
     *
     * @return A valid {@link Connection}.
     * @throws SQLException if the pool is uninitialized or no connections are available.
     */
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
    public void initialize(@NotNull Settings.Storage storage) {
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

        this.users = new SQLUserRepository(storage, this, executor);
        this.claims = new SQLClaimRepository(storage, this, executor);
    }

    @Override
    public void close() {
        if (dataSource == null) {
            return;
        }

        dataSource.close();
    }

    @NotNull
    @Override
    public UserRepository users() {
        return users;
    }

    @NotNull
    @Override
    public ClaimRepository claims() {
        return claims;
    }

    /**
     * Retrieves a SQL schema file from the plugin resources.
     *
     * @param database The name of the database file (without .sql extension).
     * @return The input stream for the resource, or {@code null} if not found.
     */
    protected InputStream getSchemaResource(String database) {
        return getClass().getClassLoader().getResourceAsStream("schemas/" + database + ".sql");
    }
}