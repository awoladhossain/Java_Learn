package com.example.banking.repository;

import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Connection conn, Transaction transaction);
    Optional<Transaction> findById(Connection conn, String id);
    List<Transaction> findByAccountId(Connection conn, String accountId);
    void updateStatus(Connection conn, String transactionId, TransactionStatus status, String failureReason);
}
