package com.hibiscusmc.hmcclaims.storage.impl.remote;

public class MariaDBStorage extends HikariStorage {
    @Override
    public String name() {
        return "MariaDB";
    }

    @Override
    protected String dataSourceClassName() {
        return "org.mariadb.jdbc.MariaDbDataSource";
    }

    @Override
    public void setup() {
    }
}