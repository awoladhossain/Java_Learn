package com.example.patterns.structural;

/**
 * 🛠️ Structural Pattern: Adapter
 * 
 * Converts the interface of a class into another interface clients expect.
 * Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.
 * E.g., Adapting legacy payment gateway API to standard enterprise PaymentProcessor interface.
 */
public class AdapterPattern {

    // Target Interface expected by modern application logic
    public interface PaymentProcessor {
        String processPayment(String accountId, double amountInDollars);
    }

    // Adaptee: Legacy third-party library with an incompatible signature
    public static class LegacyPaymentGateway {
        public boolean executeTransaction(long accountCode, long amountCents) {
            // Legacy system expects account as long code and amount in integer cents
            return accountCode > 0 && amountCents > 0;
        }
    }

    // Adapter: Implements target interface and wraps Adaptee instance
    public static class LegacyPaymentAdapter implements PaymentProcessor {
        private final LegacyPaymentGateway legacyGateway;

        public LegacyPaymentAdapter(LegacyPaymentGateway legacyGateway) {
            this.legacyGateway = legacyGateway;
        }

        @Override
        public String processPayment(String accountId, double amountInDollars) {
            // Convert string accountId to numeric code
            long accountCode = Math.abs(accountId.hashCode());
            // Convert double dollars to long cents
            long amountCents = Math.round(amountInDollars * 100);

            boolean success = legacyGateway.executeTransaction(accountCode, amountCents);
            if (success) {
                return String.format("SUCCESS: Legacy payment processed for account %s ($%.2f)", accountId, amountInDollars);
            } else {
                return String.format("FAILED: Legacy payment rejected for account %s", accountId);
            }
        }
    }
}
