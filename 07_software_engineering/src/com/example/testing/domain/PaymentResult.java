package com.example.testing.domain;

import java.time.Instant;

public record PaymentResult(boolean success, String transactionId, String errorMessage, Instant timestamp) {
    public static PaymentResult successful(String transactionId) {
        return new PaymentResult(true, transactionId, null, Instant.now());
    }

    public static PaymentResult failed(String errorMessage) {
        return new PaymentResult(false, null, errorMessage, Instant.now());
    }
}
