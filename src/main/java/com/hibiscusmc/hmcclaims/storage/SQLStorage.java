package com.hibiscusmc.hmcclaims.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLStorage implements Storage {

    public abstract Connection getConnection() throws SQLException;

    @Override
    public void logTables() {
        try {
            Connection connection = getConnection();

            try (PreparedStatement statement = connection.prepareStatement("SHOW TABLES")) {
                try (ResultSet res = statement.executeQuery()) {
                    if (res.next()) {
                        System.out.println(res.getString(1));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}