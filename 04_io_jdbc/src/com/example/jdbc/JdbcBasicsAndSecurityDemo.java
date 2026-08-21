package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Section 4.3.1: JDBC Architecture & SQL Injection Prevention.
 * 
 * Demonstrates:
 * - Core JDBC Interface Components: DriverManager, Connection, Statement, PreparedStatement, ResultSet.
 * - Vulnerable Statement concatenation allowing SQL Injection authentication bypass (' OR '1'='1).
 * - Safe PreparedStatement parameterized queries protecting database execution.
 */
public class JdbcBasicsAndSecurityDemo {

    private static final String JDBC_URL = "jdbc:h2:mem:basics_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.3.1 JDBC ARCHITECTURE & SQL INJECTION PREVENTION");
        System.out.println("------------------------------------------------------------------------");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            System.out.println("Connected to In-Memory H2 Database via DriverManager.");
            System.out.println("JDBC Driver: " + conn.getMetaData().getDriverName() + " (v" + conn.getMetaData().getDriverVersion() + ")");

            // 1. Setup Database Schema
            setupSchemaAndSeedData(conn);

            // 2. Vulnerable Statement SQL Injection Demonstration
            System.out.println("\n--- 1. Vulnerable Statement (SQL Injection Hazard) ---");
            String maliciousUserInput = "admin' -- "; // Malicious SQL injection payload with comment operator '--'
            String dummyPassword = "anything";

            demonstrateVulnerableStatement(conn, maliciousUserInput, dummyPassword);

            // 3. Safe PreparedStatement Parameterized Query
            System.out.println("\n--- 2. Safe PreparedStatement (Parameterized Queries) ---");
            demonstrateSafePreparedStatement(conn, maliciousUserInput, dummyPassword);

            // 4. Safe Authentication with Valid Credentials
            demonstrateSafePreparedStatement(conn, "alice_sre", "SecretP@ss1");

        } catch (SQLException e) {
            System.err.println("JDBC Basics Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupSchemaAndSeedData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("""
                CREATE TABLE users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL
                )
            """);

            stmt.executeUpdate("INSERT INTO users (username, password_hash, role) VALUES ('alice_sre', 'SecretP@ss1', 'ADMIN')");
            stmt.executeUpdate("INSERT INTO users (username, password_hash, role) VALUES ('bob_dev', 'DevP@ss2', 'USER')");
            System.out.println("Seeded database schema with 2 initial user records.");
        }
    }

    private static void demonstrateVulnerableStatement(Connection conn, String inputUser, String inputPass) throws SQLException {
        // HAZARD: Direct string concatenation in SQL query
        String sql = "SELECT * FROM users WHERE username = '" + inputUser + "' AND password_hash = '" + inputPass + "'";
        System.out.println("Executing Raw Query: " + sql);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  🔥 BYPASSED AUTH! Found User: ID=%d, User='%s', Role='%s'\n",
                        rs.getInt("id"), rs.getString("username"), rs.getString("role"));
            }
            if (count > 0) {
                System.out.println("🚨 CRITICAL SECURITY VULNERABILITY: Malicious payload '" + inputUser + "' compromised authentication!");
            }
        }
    }

    private static void demonstrateSafePreparedStatement(Connection conn, String inputUser, String inputPass) throws SQLException {
        // SAFE: Parameterized query placeholder '?'
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Bind parameters cleanly (separates SQL command logic from data)
            pstmt.setString(1, inputUser);
            pstmt.setString(2, inputPass);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean authenticated = false;
                while (rs.next()) {
                    authenticated = true;
                    System.out.printf("  ✅ AUTH SUCCESSFUL: Welcome %s (Role: %s)\n",
                            rs.getString("username"), rs.getString("role"));
                }
                if (!authenticated) {
                    System.out.println("  🛡️ AUTH REJECTED: User '" + inputUser + "' failed authentication cleanly.");
                }
            }
        }
    }
}
