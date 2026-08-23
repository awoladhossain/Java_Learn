package com.example.testing.domain;

public class PaymentProcessor {

    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;

    public PaymentProcessor(PaymentGateway paymentGateway, NotificationService notificationService, OrderRepository orderRepository) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    public PaymentResult executePayment(String orderId, String paymentToken) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment token cannot be null or empty");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.status() == OrderStatus.PAID) {
            throw new IllegalStateException("Order is already paid: " + orderId);
        }

        try {
            PaymentResult result = paymentGateway.processPayment(orderId, order.amount(), paymentToken);
            if (result.success()) {
                orderRepository.updateStatus(orderId, OrderStatus.PAID);
                notificationService.sendPaymentSuccessNotification(order.customerId(), orderId, result.transactionId());
            } else {
                orderRepository.updateStatus(orderId, OrderStatus.FAILED);
                notificationService.sendPaymentFailureNotification(order.customerId(), orderId, result.errorMessage());
            }
            return result;
        } catch (Exception e) {
            orderRepository.updateStatus(orderId, OrderStatus.FAILED);
            notificationService.sendPaymentFailureNotification(order.customerId(), orderId, e.getMessage());
            throw new PaymentFailedException("Payment execution encountered critical error: " + e.getMessage());
        }
    }
}
