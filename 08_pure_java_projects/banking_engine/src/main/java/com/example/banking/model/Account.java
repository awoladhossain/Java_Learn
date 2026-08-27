package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Account(
        String id,
        String customerId,
        String accountNumber,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        long version,
        Instant createdAt
) {
    public Account {
        Objects.requireNonNull(id, "Account ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(accountNumber, "Account number cannot be null");
        Objects.requireNonNull(balance, "Balance cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(id, customerId, accountNumber, newBalance, currency, status, version + 1, createdAt);
    }

    public Account withStatus(AccountStatus newStatus) {
        return new Account(id, customerId, accountNumber, balance, currency, newStatus, version + 1, createdAt);
    }
}
