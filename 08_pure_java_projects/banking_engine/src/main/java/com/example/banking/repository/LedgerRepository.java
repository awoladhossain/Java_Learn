package com.example.banking.repository;

import com.example.banking.model.LedgerEntry;

import java.sql.Connection;
import java.util.List;

public interface LedgerRepository {
    LedgerEntry save(Connection conn, LedgerEntry entry);
    List<LedgerEntry> findByAccountId(Connection conn, String accountId);
    List<LedgerEntry> findByTransactionId(Connection conn, String transactionId);
}
