package com.example.httpserver.persistence;

import com.example.httpserver.log.LogEntry;
import com.example.httpserver.log.LogLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LogRepository {
    private final DataSource dataSource;

    public LogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS server_logs (
                id VARCHAR(64) PRIMARY KEY,
                log_timestamp TIMESTAMP NOT NULL,
                log_level VARCHAR(16) NOT NULL,
                client_ip VARCHAR(64),
                http_method VARCHAR(16),
                endpoint VARCHAR(255),
                status_code INT,
                response_time_ms BIGINT,
                message TEXT
            );
            """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveBatch(List<LogEntry> entries) throws SQLException {
        if (entries == null || entries.isEmpty()) return;

        String sql = """
            INSERT INTO server_logs (id, log_timestamp, log_level, client_ip, http_method, endpoint, status_code, response_time_ms, message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (LogEntry entry : entries) {
                    pstmt.setString(1, entry.id());
                    pstmt.setTimestamp(2, Timestamp.from(entry.timestamp()));
                    pstmt.setString(3, entry.level().name());
                    pstmt.setString(4, entry.clientIp());
                    pstmt.setString(5, entry.method());
                    pstmt.setString(6, entry.endpoint());
                    pstmt.setInt(7, entry.statusCode());
                    pstmt.setLong(8, entry.responseTimeMs());
                    pstmt.setString(9, entry.message());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<LogEntry> findAll(int limit) throws SQLException {
        String sql = "SELECT id, log_timestamp, log_level, client_ip, http_method, endpoint, status_code, response_time_ms, message FROM server_logs ORDER BY log_timestamp DESC LIMIT ?";
        List<LogEntry> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new LogEntry(
                            rs.getString("id"),
                            rs.getTimestamp("log_timestamp").toInstant(),
                            LogLevel.fromString(rs.getString("log_level")),
                            rs.getString("client_ip"),
                            rs.getString("http_method"),
                            rs.getString("endpoint"),
                            rs.getInt("status_code"),
                            rs.getLong("response_time_ms"),
                            rs.getString("message")
                    ));
                }
            }
        }
        return result;
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM server_logs";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
}
