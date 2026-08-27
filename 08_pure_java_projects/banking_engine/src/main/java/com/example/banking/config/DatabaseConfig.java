package com.example.banking.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static HikariDataSource dataSource;

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            String jdbcUrl = System.getProperty("db.url",
                    System.getenv().getOrDefault("DB_URL", "jdbc:h2:mem:bankingdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"));
            String driverClass = System.getProperty("db.driver",
                    System.getenv().getOrDefault("DB_DRIVER", "org.h2.Driver"));
            String username = System.getProperty("db.username",
                    System.getenv().getOrDefault("DB_USERNAME", "sa"));
            String password = System.getProperty("db.password",
                    System.getenv().getOrDefault("DB_PASSWORD", ""));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName(driverClass);
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.setPoolName("BankingEnginePool");

            log.info("Initializing HikariCP Data Source: {}", jdbcUrl);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static void initializeDatabase(DataSource ds) {
        log.info("Initializing database schema...");
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                throw new IllegalStateException("schema.sql resource file not found");
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            log.info("Database schema initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            log.info("HikariCP Data Source closed.");
        }
    }
}
