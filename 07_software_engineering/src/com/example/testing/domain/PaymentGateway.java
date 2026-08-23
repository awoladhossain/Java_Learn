package com.example.testing.domain;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult processPayment(String orderId, BigDecimal amount, String paymentToken);
}
