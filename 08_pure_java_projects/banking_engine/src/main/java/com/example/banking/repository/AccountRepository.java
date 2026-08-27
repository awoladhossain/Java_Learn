package com.example.banking.repository;

import com.example.banking.model.Account;
import com.example.banking.model.AccountStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

public interface AccountRepository {
    Account save(Connection conn, Account account);
    Optional<Account> findById(Connection conn, String id);
    Optional<Account> findByIdForUpdate(Connection conn, String id);
    Optional<Account> findByAccountNumber(Connection conn, String accountNumber);
    void updateBalance(Connection conn, String accountId, BigDecimal newBalance, long expectedVersion);
    void updateStatus(Connection conn, String accountId, AccountStatus status);
}
