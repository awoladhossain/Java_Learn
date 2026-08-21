package com.example.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Section 4.3.3: Database Connection Pooling from Scratch & HikariCP.
 * 
 * Demonstrates:
 * - Custom Thread-Safe Connection Pool built from scratch using ArrayBlockingQueue.
 * - Standalone HikariCP Production Connection Pool configuration.
 * - Performance Benchmark: Unpooled DriverManager vs Custom Pool vs HikariCP.
 */
public class CustomAndHikariConnectionPoolDemo {

    private static final String JDBC_URL = "jdbc:h2:mem:pool_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    /**
     * Custom Thread-Safe Connection Pool Implementation from Scratch.
     */
    public static class CustomSimpleConnectionPool implements AutoCloseable {
        private final BlockingQueue<Connection> pool;
        private final int poolCapacity;

        public CustomSimpleConnectionPool(String url, String user, String pass, int poolCapacity) throws SQLException {
            this.poolCapacity = poolCapacity;
            this.pool = new ArrayBlockingQueue<>(poolCapacity);

            for (int i = 0; i < poolCapacity; i++) {
                Connection conn = DriverManager.getConnection(url, user, pass);
                pool.offer(conn);
            }
        }

        public Connection getConnection(long timeoutMs) throws InterruptedException {
            Connection conn = pool.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (conn == null) {
                throw new IllegalStateException("Connection pool exhausted! Timed out waiting for available connection.");
            }
            return conn;
        }

        public void releaseConnection(Connection conn) {
            if (conn != null) {
                pool.offer(conn);
            }
        }

        public int getAvailableCount() {
            return pool.size();
        }

        public int getPoolCapacity() {
            return poolCapacity;
        }

        @Override
        public void close() {
            for (Connection conn : pool) {
                try {
                    if (!conn.isClosed()) conn.close();
                } catch (SQLException ignored) {}
            }
            pool.clear();
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.3.3 CONNECTION POOLING FROM SCRATCH & HIKARICP");
        System.out.println("------------------------------------------------------------------------");

        try {
            // Setup Schema
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS metrics (id INT PRIMARY KEY AUTO_INCREMENT, val INT)");
            }

            int iterations = 100;

            // 1. Unpooled DriverManager Execution Benchmark
            System.out.println("\n--- 1. Benchmark: Direct Unpooled DriverManager ---");
            long startUnpooled = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    rs.next();
                }
            }
            long timeUnpooled = System.currentTimeMillis() - startUnpooled;
            System.out.printf("  DriverManager Execution Time (%d queries): %d ms\n", iterations, timeUnpooled);

            // 2. Custom Connection Pool Execution Benchmark
            System.out.println("\n--- 2. Benchmark: Custom Connection Pool Built From Scratch ---");
            long startCustomPool = System.currentTimeMillis();
            try (CustomSimpleConnectionPool customPool = new CustomSimpleConnectionPool(JDBC_URL, DB_USER, DB_PASS, 10)) {
                System.out.println("  Custom Pool Initialized (Available: " + customPool.getAvailableCount() + ")");
                for (int i = 0; i < iterations; i++) {
                    Connection conn = customPool.getConnection(1000);
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT 1")) {
                        rs.next();
                    } finally {
                        customPool.releaseConnection(conn); // Return connection back to pool
                    }
                }
            }
            long timeCustomPool = System.currentTimeMillis() - startCustomPool;
            System.out.printf("  Custom Pool Execution Time  (%d queries): %d ms\n", iterations, timeCustomPool);

            // 3. Standalone HikariCP Connection Pool Execution Benchmark
            System.out.println("\n--- 3. Benchmark: Standalone Production-Grade HikariCP Pool ---");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASS);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(2000);
            config.setPoolName("ProductionSRE-HikariPool");

            long startHikari = System.currentTimeMillis();
            try (HikariDataSource hikariDs = new HikariDataSource(config)) {
                System.out.println("  HikariCP Pool Active: " + hikariDs.getPoolName());
                for (int i = 0; i < iterations; i++) {
                    try (Connection conn = hikariDs.getConnection(); // Auto-returns connection to Hikari pool on close()
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT 1")) {
                        rs.next();
                    }
                }
            }
            long timeHikari = System.currentTimeMillis() - startHikari;
            System.out.printf("  HikariCP Execution Time    (%d queries): %d ms\n", iterations, timeHikari);

            double speedup = timeUnpooled > 0 ? (double) timeUnpooled / Math.max(1, timeHikari) : 1.0;
            System.out.printf("🚀 Connection Pooling Speedup Ratio : %.2fx Faster than unpooled connections!\n", speedup);

        } catch (Exception e) {
            System.err.println("Connection Pool Demo Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n💡 Senior SRE Production Rule:");
        System.out.println("   NEVER use DriverManager.getConnection() in high-throughput production code!");
        System.out.println("   Establishing a physical TCP/database connection involves handshake overhead (10-50ms).");
        System.out.println("   HikariCP maintains warm connection pools with microsecond-level connection acquisition overhead!");
    }
}
