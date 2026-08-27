package com.example.banking.repository;

import com.example.banking.model.LedgerEntry;
import com.example.banking.model.LedgerEntryType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LedgerRepositoryImpl implements LedgerRepository {

    @Override
    public LedgerEntry save(Connection conn, LedgerEntry entry) {
        String sql = "INSERT INTO ledger_entries (id, transaction_id, account_id, entry_type, amount, balance_after, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.id());
            pstmt.setString(2, entry.transactionId());
            pstmt.setString(3, entry.accountId());
            pstmt.setString(4, entry.entryType().name());
            pstmt.setBigDecimal(5, entry.amount());
            pstmt.setBigDecimal(6, entry.balanceAfter());
            pstmt.setTimestamp(7, Timestamp.from(entry.createdAt()));
            pstmt.executeUpdate();
            return entry;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ledger entry: " + entry.id(), e);
        }
    }

    @Override
    public List<LedgerEntry> findByAccountId(Connection conn, String accountId) {
        String sql = "SELECT id, transaction_id, account_id, entry_type, amount, balance_after, created_at " +
                "FROM ledger_entries WHERE account_id = ? ORDER BY created_at ASC";
        return queryLedgerEntries(conn, sql, accountId);
    }

    @Override
    public List<LedgerEntry> findByTransactionId(Connection conn, String transactionId) {
        String sql = "SELECT id, transaction_id, account_id, entry_type, amount, balance_after, created_at " +
                "FROM ledger_entries WHERE transaction_id = ? ORDER BY created_at ASC";
        return queryLedgerEntries(conn, sql, transactionId);
    }

    private List<LedgerEntry> queryLedgerEntries(Connection conn, String sql, String param) {
        List<LedgerEntry> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLedgerEntry(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query ledger entries with param: " + param, e);
        }
        return list;
    }

    private LedgerEntry mapResultSetToLedgerEntry(ResultSet rs) throws SQLException {
        return new LedgerEntry(
                rs.getString("id"),
                rs.getString("transaction_id"),
                rs.getString("account_id"),
                LedgerEntryType.valueOf(rs.getString("entry_type")),
                rs.getBigDecimal("amount"),
                rs.getBigDecimal("balance_after"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
