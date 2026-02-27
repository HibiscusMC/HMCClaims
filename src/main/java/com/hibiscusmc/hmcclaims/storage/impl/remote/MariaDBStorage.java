package com.hibiscusmc.hmcclaims.storage.impl.remote;

import com.hibiscusmc.hmcclaims.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
        String databaseName = name();

        try (Connection connection = getConnection()) {
            InputStream rawSchema = getSchemaResource(databaseName.toLowerCase());
            if (rawSchema == null) {
                throw new IllegalStateException("Schema not found for database " + databaseName);
            }

            String schema = new String(rawSchema.readAllBytes(), StandardCharsets.UTF_8);

            for (String rawStatement : schema.split(";")) {
                String statement = rawStatement.trim()
                        .replaceAll("\\s+", " ")
                        .replaceAll("\\{prefix}", storage.prefix());

                if (statement.isEmpty()) {
                    continue;
                }

                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            rawSchema.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}