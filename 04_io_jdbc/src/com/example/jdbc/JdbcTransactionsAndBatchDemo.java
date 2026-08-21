package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Section 4.3.2: Transaction Management & Batch Processing in Pure Java.
 * 
 * Demonstrates:
 * - Manual Transaction Control (connection.setAutoCommit(false), connection.commit(), connection.rollback()).
 * - Atomic Multi-table Banking Transfer & Fault Rollback.
 * - Batch Processing (preparedStatement.addBatch(), executeBatch()) for high-throughput bulk inserts.
 */
public class JdbcTransactionsAndBatchDemo {

    private static final String JDBC_URL = "jdbc:h2:mem:tx_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.3.2 TRANSACTION MANAGEMENT & BATCH PROCESSING");
        System.out.println("------------------------------------------------------------------------");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            setupSchemaAndAccounts(conn);

            // 1. Transaction Failure Simulation with Rollback
            System.out.println("\n--- 1. Transaction Management: Failure Scenario & Rollback ---");
            printAccountBalances(conn, "Initial Balances Before Attempted Transfer");
            transferMoneyWithRollbackSimulated(conn, "ACC-101", "ACC-102", 300.0, true);
            printAccountBalances(conn, "Balances After Failed Transfer (EXPECTED UNCHANGED)");

            // 2. Successful Transaction Execution with Commit
            System.out.println("\n--- 2. Transaction Management: Successful Commit ---");
            transferMoneyWithRollbackSimulated(conn, "ACC-101", "ACC-102", 300.0, false);
            printAccountBalances(conn, "Balances After Successful Commit (ATOMIC UPDATE)");

            // 3. Batch Processing Execution & Performance Benchmark
            System.out.println("\n--- 3. Batch Processing (addBatch() / executeBatch()) ---");
            benchmarkBatchInserts(conn, 1000);

        } catch (SQLException e) {
            System.err.println("Transaction Demo Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupSchemaAndAccounts(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS accounts");
            stmt.execute("DROP TABLE IF EXISTS audit_logs");
            stmt.execute("""
                CREATE TABLE accounts (
                    account_number VARCHAR(20) PRIMARY KEY,
                    holder_name VARCHAR(50) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE audit_logs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    event_type VARCHAR(50) NOT NULL,
                    message VARCHAR(255) NOT NULL
                )
            """);

            stmt.executeUpdate("INSERT INTO accounts VALUES ('ACC-101', 'Alice Operations', 1000.00)");
            stmt.executeUpdate("INSERT INTO accounts VALUES ('ACC-102', 'Bob Reserves', 500.00)");
        }
    }

    private static void transferMoneyWithRollbackSimulated(Connection conn, String fromAcc, String toAcc, double amount, boolean simulateFailure) throws SQLException {
        String deductSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String addSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

        // Disable Auto-Commit mode to start manual transaction boundary
        conn.setAutoCommit(false);

        try (PreparedStatement deductStmt = conn.prepareStatement(deductSql);
             PreparedStatement addStmt = conn.prepareStatement(addSql)) {

            // Step 1: Deduct from source account
            deductStmt.setDouble(1, amount);
            deductStmt.setString(2, fromAcc);
            int rows1 = deductStmt.executeUpdate();

            System.out.printf("  Step 1: Deducted $%.2f from %s (Rows affected: %d)\n", amount, fromAcc, rows1);

            // Simulate middle transaction error (e.g. network failure / crash)
            if (simulateFailure) {
                throw new RuntimeException("🔥 SIMULATED NETWORK OUTAGE DURING TRANSFER!");
            }

            // Step 2: Add to destination account
            addStmt.setDouble(1, amount);
            addStmt.setString(2, toAcc);
            int rows2 = addStmt.executeUpdate();

            System.out.printf("  Step 2: Credited $%.2f to %s (Rows affected: %d)\n", amount, toAcc, rows2);

            // Step 3: Explicit Transaction Commit
            conn.commit();
            System.out.println("  ✅ TRANSACTION COMMITTED SUCCESSFULLY!");

        } catch (Exception e) {
            // STEP 4: Rollback Transaction on any failure
            conn.rollback();
            System.err.println("  🛡️ TRANSACTION ROLLED BACK! Cause: " + e.getMessage());
        } finally {
            // Re-enable auto-commit for connection pool cleanliness
            conn.setAutoCommit(true);
        }
    }

    private static void printAccountBalances(Connection conn, String label) throws SQLException {
        System.out.println("  [" + label + "]:");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM accounts ORDER BY account_number")) {
            while (rs.next()) {
                System.out.printf("    - %s (%s) : $%.2f\n",
                        rs.getString("account_number"), rs.getString("holder_name"), rs.getDouble("balance"));
            }
        }
    }

    private static void benchmarkBatchInserts(Connection conn, int totalRecords) throws SQLException {
        String sql = "INSERT INTO audit_logs (event_type, message) VALUES (?, ?)";

        // 1. Single Statement Executions
        long startSingle = System.currentTimeMillis();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < totalRecords; i++) {
                pstmt.setString(1, "METRIC_LOG");
                pstmt.setString(2, "Single insert log payload #" + i);
                pstmt.executeUpdate();
            }
        }
        long timeSingle = System.currentTimeMillis() - startSingle;
        System.out.printf("  Single Insert Time (%d records) : %d ms\n", totalRecords, timeSingle);

        // Clear log table
        try (Statement stmt = conn.createStatement()) { stmt.executeUpdate("DELETE FROM audit_logs"); }

        // 2. Batch Execution
        long startBatch = System.currentTimeMillis();
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < totalRecords; i++) {
                pstmt.setString(1, "METRIC_LOG");
                pstmt.setString(2, "Batch insert log payload #" + i);
                pstmt.addBatch(); // Add to query batch memory

                if (i % 250 == 0 || i == totalRecords - 1) {
                    pstmt.executeBatch(); // Send batch query block to database engine
                }
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
        long timeBatch = System.currentTimeMillis() - startBatch;
        System.out.printf("  Batch Insert Time  (%d records) : %d ms\n", totalRecords, timeBatch);

        double speedup = timeSingle > 0 ? (double) timeSingle / Math.max(1, timeBatch) : 1.0;
        System.out.printf("🚀 Batch Processing Speedup Ratio : %.2fx Faster!\n", speedup);
    }
}
