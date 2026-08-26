package com.example.patterns.structural;

/**
 * 🛠️ Structural Pattern: Facade
 * 
 * Provides a unified interface to a set of interfaces in a subsystem.
 * Facade defines a higher-level interface that makes the subsystem easier to use.
 * E.g., OrderCheckoutFacade wrapping Inventory, Payment, Shipping, and Email subsystems.
 */
public class FacadePattern {

    // Subsystem 1: Inventory
    public static class InventoryService {
        public boolean checkStock(String itemId, int quantity) {
            return quantity <= 100;
        }
        public void reserveStock(String itemId, int quantity) {
            // Logic to reserve item stock
        }
    }

    // Subsystem 2: Payment
    public static class PaymentGateway {
        public boolean charge(String customerId, double amount) {
            return amount > 0;
        }
    }

    // Subsystem 3: Logistics / Shipping
    public static class ShippingService {
        public String createShipment(String itemId, int quantity, String address) {
            return "TRACK-" + Math.abs((itemId + address).hashCode());
        }
    }

    // Subsystem 4: Notification
    public static class EmailNotificationService {
        public String sendOrderConfirmation(String customerId, String trackingCode) {
            return String.format("Confirmation email sent to %s with tracking %s", customerId, trackingCode);
        }
    }

    // Facade Object
    public static class OrderCheckoutFacade {
        private final InventoryService inventoryService;
        private final PaymentGateway paymentGateway;
        private final ShippingService shippingService;
        private final EmailNotificationService notificationService;

        public OrderCheckoutFacade(InventoryService inventoryService,
                                   PaymentGateway paymentGateway,
                                   ShippingService shippingService,
                                   EmailNotificationService notificationService) {
            this.inventoryService = inventoryService;
            this.paymentGateway = paymentGateway;
            this.shippingService = shippingService;
            this.notificationService = notificationService;
        }

        public String placeOrder(String customerId, String itemId, int quantity, double price, String shippingAddress) {
            if (!inventoryService.checkStock(itemId, quantity)) {
                return "ORDER FAILED: Item out of stock";
            }

            double totalAmount = price * quantity;
            if (!paymentGateway.charge(customerId, totalAmount)) {
                return "ORDER FAILED: Payment declined";
            }

            inventoryService.reserveStock(itemId, quantity);
            String trackingCode = shippingService.createShipment(itemId, quantity, shippingAddress);
            notificationService.sendOrderConfirmation(customerId, trackingCode);

            return String.format("ORDER SUCCESSFUL [Tracking: %s]", trackingCode);
        }
    }
}
