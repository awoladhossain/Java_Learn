package com.example.jdbc;

/**
 * Main Runner Class for Phase 4.3: Native JDBC (Java Database Connectivity) Deep-Dive.
 * 
 * Executes comprehensive demonstrations covering:
 * - 4.3.1 JDBC Architecture (DriverManager, Connection, Statement, PreparedStatement, ResultSet) & SQL Injection Prevention.
 * - 4.3.2 Transaction Management in Pure Java (setAutoCommit(false), commit(), rollback()) & Batch Processing.
 * - 4.3.3 Database Connection Pooling from Scratch & Standalone HikariCP Integration.
 */
public class JdbcMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 4.3: NATIVE JDBC (JAVA DATABASE CONNECTIVITY) DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. JDBC Basics & SQL Injection Prevention
        JdbcBasicsAndSecurityDemo.runDemo();

        // 2. Transaction Management & Batch Processing
        JdbcTransactionsAndBatchDemo.runDemo();

        // 3. Connection Pooling from Scratch & Standalone HikariCP
        CustomAndHikariConnectionPoolDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 4.3 NATIVE JDBC EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
