package com.example.testing.domain;

import java.math.BigDecimal;

public record Order(String id, String customerId, BigDecimal amount, OrderStatus status) {
    public Order {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than zero");
        }
    }
}
