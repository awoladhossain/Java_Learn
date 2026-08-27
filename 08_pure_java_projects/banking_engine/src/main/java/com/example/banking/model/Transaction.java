package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Transaction(
        String id,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String referenceCode,
        String failureReason,
        Instant createdAt
) {
    public Transaction {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(referenceCode, "Reference code cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Transaction withStatus(TransactionStatus newStatus, String reason) {
        return new Transaction(id, sourceAccountId, targetAccountId, amount, type, newStatus, referenceCode, reason, createdAt);
    }
}
