package com.example.banking.model;

import java.time.Instant;
import java.util.Objects;

public record Customer(
        String id,
        String name,
        String email,
        Instant createdAt
) {
    public Customer {
        Objects.requireNonNull(id, "Customer ID cannot be null");
        Objects.requireNonNull(name, "Customer name cannot be null");
        Objects.requireNonNull(email, "Customer email cannot be null");
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
