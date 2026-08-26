package com.example.patterns.behavioral;

/**
 * 🛠️ Behavioral Pattern: Strategy
 * 
 * Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
 * Strategy lets the algorithm vary independently from clients that use it.
 * E.g., Dynamic discount computation strategies in an e-commerce checkout context.
 */
public class StrategyPattern {

    // Strategy Interface
    public interface PaymentStrategy {
        String pay(double amount);
    }

    // Concrete Strategy 1: Credit Card
    public static class CreditCardStrategy implements PaymentStrategy {
        private final String cardNumber;

        public CreditCardStrategy(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        @Override
        public String pay(double amount) {
            return String.format("Paid $%.2f using Credit Card ending in %s", amount, cardNumber.substring(cardNumber.length() - 4));
        }
    }

    // Concrete Strategy 2: PayPal
    public static class PayPalStrategy implements PaymentStrategy {
        private final String email;

        public PayPalStrategy(String email) {
            this.email = email;
        }

        @Override
        public String pay(double amount) {
            return String.format("Paid $%.2f via PayPal account (%s)", amount, email);
        }
    }

    // Concrete Strategy 3: Crypto / Web3
    public static class CryptoStrategy implements PaymentStrategy {
        private final String walletAddress;

        public CryptoStrategy(String walletAddress) {
            this.walletAddress = walletAddress;
        }

        @Override
        public String pay(double amount) {
            return String.format("Paid $%.2f using Crypto wallet %s...", amount, walletAddress.substring(0, 6));
        }
    }

    // Context Class
    public static class ShoppingCart {
        private PaymentStrategy paymentStrategy;

        public void setPaymentStrategy(PaymentStrategy strategy) {
            this.paymentStrategy = strategy;
        }

        public String checkout(double totalAmount) {
            if (paymentStrategy == null) {
                throw new IllegalStateException("Payment strategy not set!");
            }
            return paymentStrategy.pay(totalAmount);
        }
    }
}
