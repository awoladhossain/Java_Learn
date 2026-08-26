package com.example.httpserver.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    private static HikariDataSource dataSource;

    public static synchronized DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:logserver_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");

            // Pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000);
            config.setPoolName("Hikari-LogServer-Pool");

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
