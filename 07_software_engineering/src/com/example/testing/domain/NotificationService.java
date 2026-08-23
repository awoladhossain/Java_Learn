package com.example.testing.domain;

public interface NotificationService {
    void sendPaymentSuccessNotification(String customerId, String orderId, String transactionId);
    void sendPaymentFailureNotification(String customerId, String orderId, String reason);
}
