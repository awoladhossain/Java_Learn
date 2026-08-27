package com.example.banking.repository;

import com.example.banking.exception.ConcurrentTransactionException;
import com.example.banking.model.Account;
import com.example.banking.model.AccountStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Account save(Connection conn, Account account) {
        String sql = "INSERT INTO accounts (id, customer_id, account_number, balance, currency, status, version, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.id());
            pstmt.setString(2, account.customerId());
            pstmt.setString(3, account.accountNumber());
            pstmt.setBigDecimal(4, account.balance());
            pstmt.setString(5, account.currency());
            pstmt.setString(6, account.status().name());
            pstmt.setLong(7, account.version());
            pstmt.setTimestamp(8, Timestamp.from(account.createdAt()));
            pstmt.executeUpdate();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save account: " + account.id(), e);
        }
    }

    @Override
    public Optional<Account> findById(Connection conn, String id) {
        String sql = "SELECT id, customer_id, account_number, balance, currency, status, version, created_at " +
                "FROM accounts WHERE id = ?";
        return queryAccount(conn, sql, id);
    }

    @Override
    public Optional<Account> findByIdForUpdate(Connection conn, String id) {
        String sql = "SELECT id, customer_id, account_number, balance, currency, status, version, created_at " +
                "FROM accounts WHERE id = ? FOR UPDATE";
        return queryAccount(conn, sql, id);
    }

    @Override
    public Optional<Account> findByAccountNumber(Connection conn, String accountNumber) {
        String sql = "SELECT id, customer_id, account_number, balance, currency, status, version, created_at " +
                "FROM accounts WHERE account_number = ?";
        return queryAccount(conn, sql, accountNumber);
    }

    @Override
    public void updateBalance(Connection conn, String accountId, BigDecimal newBalance, long expectedVersion) {
        String sql = "UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, accountId);
            pstmt.setLong(3, expectedVersion);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows == 0) {
                throw new ConcurrentTransactionException(
                        "Optimistic lock failure or record missing for account ID: " + accountId + " (version: " + expectedVersion + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update balance for account: " + accountId, e);
        }
    }

    @Override
    public void updateStatus(Connection conn, String accountId, AccountStatus status) {
        String sql = "UPDATE accounts SET status = ?, version = version + 1 WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status for account: " + accountId, e);
        }
    }

    private Optional<Account> queryAccount(Connection conn, String sql, String param) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query account with param: " + param, e);
        }
        return Optional.empty();
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getString("id"),
                rs.getString("customer_id"),
                rs.getString("account_number"),
                rs.getBigDecimal("balance"),
                rs.getString("currency"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getLong("version"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
