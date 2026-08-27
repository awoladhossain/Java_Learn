package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record LedgerEntry(
        String id,
        String transactionId,
        String accountId,
        LedgerEntryType entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {
    public LedgerEntry {
        Objects.requireNonNull(id, "Ledger Entry ID cannot be null");
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(entryType, "Entry type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(balanceAfter, "Balance after cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ledger entry amount must be positive");
        }
        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance after cannot be negative");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
