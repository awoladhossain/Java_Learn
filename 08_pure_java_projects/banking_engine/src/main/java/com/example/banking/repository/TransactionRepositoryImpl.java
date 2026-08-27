package com.example.banking.repository;

import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public Transaction save(Connection conn, Transaction tx) {
        String sql = "INSERT INTO transactions (id, source_account_id, target_account_id, amount, type, status, reference_code, failure_reason, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.id());
            pstmt.setString(2, tx.sourceAccountId());
            pstmt.setString(3, tx.targetAccountId());
            pstmt.setBigDecimal(4, tx.amount());
            pstmt.setString(5, tx.type().name());
            pstmt.setString(6, tx.status().name());
            pstmt.setString(7, tx.referenceCode());
            pstmt.setString(8, tx.failureReason());
            pstmt.setTimestamp(9, Timestamp.from(tx.createdAt()));
            pstmt.executeUpdate();
            return tx;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction: " + tx.id(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Connection conn, String id) {
        String sql = "SELECT id, source_account_id, target_account_id, amount, type, status, reference_code, failure_reason, created_at " +
                "FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByAccountId(Connection conn, String accountId) {
        String sql = "SELECT id, source_account_id, target_account_id, amount, type, status, reference_code, failure_reason, created_at " +
                "FROM transactions WHERE source_account_id = ? OR target_account_id = ? ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions for account: " + accountId, e);
        }
        return list;
    }

    @Override
    public void updateStatus(Connection conn, String transactionId, TransactionStatus status, String failureReason) {
        String sql = "UPDATE transactions SET status = ?, failure_reason = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, failureReason);
            pstmt.setString(3, transactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status for transaction: " + transactionId, e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("id"),
                rs.getString("source_account_id"),
                rs.getString("target_account_id"),
                rs.getBigDecimal("amount"),
                TransactionType.valueOf(rs.getString("type")),
                TransactionStatus.valueOf(rs.getString("status")),
                rs.getString("reference_code"),
                rs.getString("failure_reason"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
