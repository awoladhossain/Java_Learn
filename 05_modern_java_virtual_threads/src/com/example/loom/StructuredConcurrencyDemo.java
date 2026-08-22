package com.example.loom;

import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

/**
 * Section 5.4.2: Structured Concurrency (JEP 453 Preview).
 * 
 * Demonstrates:
 * - Structured Task Scopes: Treating concurrent subtasks as a single unit of work.
 * - ShutdownOnFailure: Fail-fast semantics (cancels all subtasks if any subtask throws an exception).
 * - ShutdownOnSuccess: Speculative execution (returns result of first successful subtask and cancels others).
 * - SRE Observability & Resource Protection: Preventing orphan thread leaks.
 */
public class StructuredConcurrencyDemo {

    public record UserProfile(String userId, String username, String email) {}
    public record OrderHistory(String userId, int totalOrders, double totalSpent) {}
    public record UserDashboard(UserProfile profile, OrderHistory orders) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.4.2 STRUCTURED CONCURRENCY (JEP 453 Preview)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. ShutdownOnFailure (Fail-Fast Pattern)
        // ==========================================
        System.out.println("\n--- 1. StructuredTaskScope.ShutdownOnFailure ---");

        try {
            UserDashboard dashboard = fetchUserDashboard("user-7782");
            System.out.println("Dashboard Assembly Successful:");
            System.out.println("   Profile: " + dashboard.profile());
            System.out.println("   Orders : " + dashboard.orders());
        } catch (Exception e) {
            System.err.println("Failed to assemble user dashboard: " + e.getMessage());
        }

        // ==========================================
        // 2. ShutdownOnSuccess (Speculative Execution Pattern)
        // ==========================================
        System.out.println("\n--- 2. StructuredTaskScope.ShutdownOnSuccess (Fastest Mirror Query) ---");

        try {
            String exchangeRate = fetchFastestExchangeRate("USD", "EUR");
            System.out.println("Fastest Exchange Rate Received: " + exchangeRate);
        } catch (Exception e) {
            System.err.println("Failed to fetch exchange rate: " + e.getMessage());
        }

        System.out.println("\n💡 SRE Insight: Structured Concurrency guarantees that when a parent scope exits, no orphan virtual threads");
        System.out.println("   are left running in the background, eliminating memory leaks and unmonitored background tasks.");
    }

    /**
     * Fetches user profile and order history concurrently using ShutdownOnFailure scope.
     */
    private static UserDashboard fetchUserDashboard(String userId) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            // Fork subtask 1: Fetch User Profile
            StructuredTaskScope.Subtask<UserProfile> profileTask = scope.fork(() -> {
                System.out.println("   [SUBTASK 1] Fetching User Profile for: " + userId);
                Thread.sleep(Duration.ofMillis(80));
                return new UserProfile(userId, "alex_developer", "alex@example.com");
            });

            // Fork subtask 2: Fetch Order History
            StructuredTaskScope.Subtask<OrderHistory> orderTask = scope.fork(() -> {
                System.out.println("   [SUBTASK 2] Fetching Order History for: " + userId);
                Thread.sleep(Duration.ofMillis(120));
                return new OrderHistory(userId, 14, 1250.75);
            });

            // Block and join both subtasks
            scope.join();
            scope.throwIfFailed(e -> new RuntimeException("Dashboard subtask failed", e));

            // Extract results safely from subtasks
            return new UserDashboard(profileTask.get(), orderTask.get());
        }
    }

    /**
     * Queries 3 mirror servers concurrently for currency rate and returns the first response.
     */
    private static String fetchFastestExchangeRate(String from, String to) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            
            // Server 1 (Latency: 200ms)
            scope.fork(() -> {
                System.out.println("   [MIRROR 1] Querying Bank API US-East...");
                Thread.sleep(Duration.ofMillis(200));
                return "1 USD = 0.92 EUR (Source: US-East)";
            });

            // Server 2 (Latency: 50ms - FASTEST!)
            scope.fork(() -> {
                System.out.println("   [MIRROR 2] Querying Bank API EU-Central...");
                Thread.sleep(Duration.ofMillis(50)); // Responds first!
                return "1 USD = 0.92 EUR (Source: EU-Central)";
            });

            // Server 3 (Latency: 300ms)
            scope.fork(() -> {
                System.out.println("   [MIRROR 3] Querying Bank API AP-South...");
                Thread.sleep(Duration.ofMillis(300));
                return "1 USD = 0.92 EUR (Source: AP-South)";
            });

            // Block until first successful result arrives, then cancel remaining tasks automatically!
            scope.join();
            return scope.result();
        }
    }
}
