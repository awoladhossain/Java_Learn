package com.example.advanced;

/**
 * Section 3.4.3: Sealed Classes & Interfaces (`sealed ... permits`).
 * 
 * Demonstrates:
 * - Sealed interface / class contracts with explicit permits clause.
 * - Subtype Modifiers: `final`, `sealed`, and `non-sealed`.
 * - Pattern Matching readiness & Exhaustive handling on domain hierarchies.
 */
public class SealedClassesDemo {

    /**
     * Root Sealed Interface defining permitted domain event implementations.
     */
    public sealed interface SecurityEvent permits UserLoginEvent, FinancialTransactionEvent, InfrastructureAlertEvent {
        String eventId();
        long timestamp();
        String getEventSummary();
    }

    /**
     * Permitted Subtype 1: `final` Record (Hierarchy Termination).
     */
    public record UserLoginEvent(
            String eventId,
            long timestamp,
            String username,
            String ipAddress,
            boolean success
    ) implements SecurityEvent {
        @Override
        public String getEventSummary() {
            return String.format("User Login [%s] from %s (Success=%b)", username, ipAddress, success);
        }
    }

    /**
     * Permitted Subtype 2: `sealed` Class (Sub-hierarchy restriction).
     * Must explicitly list its own permitted subclasses.
     */
    public abstract static sealed class FinancialTransactionEvent implements SecurityEvent permits DepositEvent, WithdrawalEvent {
        private final String eventId;
        private final long timestamp;
        private final double amount;

        public FinancialTransactionEvent(String eventId, long timestamp, double amount) {
            this.eventId = eventId;
            this.timestamp = timestamp;
            this.amount = amount;
        }

        @Override public String eventId() { return eventId; }
        @Override public long timestamp() { return timestamp; }
        public double amount() { return amount; }
    }

    public static final class DepositEvent extends FinancialTransactionEvent {
        private final String targetAccount;

        public DepositEvent(String eventId, long timestamp, double amount, String targetAccount) {
            super(eventId, timestamp, amount);
            this.targetAccount = targetAccount;
        }

        @Override
        public String getEventSummary() {
            return String.format("Deposit $%.2f into Account [%s]", amount(), targetAccount);
        }
    }

    public static final class WithdrawalEvent extends FinancialTransactionEvent {
        private final String sourceAccount;

        public WithdrawalEvent(String eventId, long timestamp, double amount, String sourceAccount) {
            super(eventId, timestamp, amount);
            this.sourceAccount = sourceAccount;
        }

        @Override
        public String getEventSummary() {
            return String.format("Withdrawal $%.2f from Account [%s]", amount(), sourceAccount);
        }
    }

    /**
     * Permitted Subtype 3: `non-sealed` Class (Re-opens hierarchy for extension).
     */
    public static non-sealed class InfrastructureAlertEvent implements SecurityEvent {
        private final String eventId;
        private final long timestamp;
        private final String serviceName;

        public InfrastructureAlertEvent(String eventId, long timestamp, String serviceName) {
            this.eventId = eventId;
            this.timestamp = timestamp;
            this.serviceName = serviceName;
        }

        @Override public String eventId() { return eventId; }
        @Override public long timestamp() { return timestamp; }
        public String serviceName() { return serviceName; }

        @Override
        public String getEventSummary() {
            return "Infrastructure Alert for Service: " + serviceName;
        }
    }

    /**
     * Custom extension allowed ONLY because InfrastructureAlertEvent is `non-sealed`.
     */
    public static class CustomThirdPartyAlertEvent extends InfrastructureAlertEvent {
        public CustomThirdPartyAlertEvent(String eventId, long timestamp, String serviceName) {
            super(eventId, timestamp, serviceName);
        }
    }

    /**
     * Processor function demonstrating pattern matching on Sealed Hierarchy.
     */
    public static String processSecurityEvent(SecurityEvent event) {
        // Pattern Matching with instanceof (Java 16+)
        if (event instanceof UserLoginEvent login) {
            return "🔐 AUTH: " + login.username() + " (IP: " + login.ipAddress() + ")";
        } else if (event instanceof DepositEvent deposit) {
            return "💰 DEPOSIT: $" + deposit.amount();
        } else if (event instanceof WithdrawalEvent withdrawal) {
            return "💸 WITHDRAWAL: $" + withdrawal.amount();
        } else if (event instanceof InfrastructureAlertEvent alert) {
            return "🚨 ALERT: " + alert.serviceName();
        }
        throw new IllegalArgumentException("Unknown security event type");
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.4.3 SEALED CLASSES & INTERFACES (sealed ... permits)");
        System.out.println("------------------------------------------------------------------------");

        // 1. Instantiating Permitted Subclasses
        System.out.println("\n--- 1. Sealed Hierarchy Subtypes & Summary Methods ---");
        SecurityEvent loginEvent = new UserLoginEvent("evt-001", System.currentTimeMillis(), "sre_admin", "10.0.0.42", true);
        SecurityEvent depositEvent = new DepositEvent("evt-002", System.currentTimeMillis(), 5000.0, "ACC-9981");
        SecurityEvent alertEvent = new InfrastructureAlertEvent("evt-003", System.currentTimeMillis(), "payment-processor");
        SecurityEvent customAlert = new CustomThirdPartyAlertEvent("evt-004", System.currentTimeMillis(), "legacy-mon-agent");

        System.out.println("Event 1: " + loginEvent.getEventSummary());
        System.out.println("Event 2: " + depositEvent.getEventSummary());
        System.out.println("Event 3: " + alertEvent.getEventSummary());
        System.out.println("Event 4: " + customAlert.getEventSummary());

        // 2. Pattern Matching Processing
        System.out.println("\n--- 2. Exhaustive Pattern Matching Processing ---");
        System.out.println("Processed Event 1 -> " + processSecurityEvent(loginEvent));
        System.out.println("Processed Event 2 -> " + processSecurityEvent(depositEvent));
        System.out.println("Processed Event 3 -> " + processSecurityEvent(alertEvent));

        // 3. Reflection Inspection of Sealed Hierarchy
        System.out.println("\n--- 3. Reflection Inspection of Sealed Interface ---");
        Class<SecurityEvent> sealedClass = SecurityEvent.class;
        System.out.println("Is SecurityEvent sealed? : " + sealedClass.isSealed());

        System.out.println("Permitted Subclasses of SecurityEvent:");
        for (Class<?> permitted : sealedClass.getPermittedSubclasses()) {
            System.out.println("  - " + permitted.getName());
        }

        System.out.println("\n💡 SRE Architecture Benefit: Sealed classes enforce domain modeling security");
        System.out.println("   by preventing unauthorized third-party extensions and enabling exhaustiveness checks at compile time!");
    }
}
