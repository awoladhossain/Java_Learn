package com.example.interfaces;

/**
 * Interface demonstrating Java 8+ and 9+ interface features:
 * 1. Interface constants (public static final implicitly)
 * 2. Default methods (defender methods providing default behavior)
 * 3. Static methods (utility methods attached to interface contract)
 * 4. Private helper methods (Java 9+ code reuse inside interface default methods)
 */
public interface ResilientService {

    // 1️⃣ Interface Fields: Automatically 'public static final'
    int DEFAULT_MAX_RETRIES = 3;
    long DEFAULT_RETRY_BACKOFF_MS = 500L;
    String PROTOCOL_VERSION = "v2.1-sre";

    // Standard abstract methods (contract to be implemented by classes)
    String getServiceName();
    boolean executeOperation(String operationName);

    // 2️⃣ Static Methods: Pure utility functions attached to the interface namespace
    static boolean isValidBackoff(long backoffMs) {
        return backoffMs >= 100L && backoffMs <= 10000L;
    }

    static String formatTelemetryEvent(String serviceName, String status, long durationMs) {
        return String.format("[%s] service=%s status=%s duration=%dms",
                PROTOCOL_VERSION, serviceName, status, durationMs);
    }

    // 3️⃣ Default Methods: Provide default fallback behavior without breaking existing implementations
    default boolean executeWithRetry(String operationName) {
        return executeWithRetry(operationName, DEFAULT_MAX_RETRIES);
    }

    default boolean executeWithRetry(String operationName, int maxRetries) {
        logRetryAttempt(operationName, 1, maxRetries);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long startTime = System.currentTimeMillis();
            try {
                boolean success = executeOperation(operationName);
                long duration = System.currentTimeMillis() - startTime;
                if (success) {
                    recordMetrics(operationName, "SUCCESS", duration);
                    return true;
                }
            } catch (Exception ex) {
                long duration = System.currentTimeMillis() - startTime;
                recordMetrics(operationName, "FAILED_ATTEMPT_" + attempt, duration);
            }

            if (attempt < maxRetries) {
                logRetryAttempt(operationName, attempt + 1, maxRetries);
                try {
                    Thread.sleep(DEFAULT_RETRY_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    default String getServiceHealthSummary() {
        return buildFormattedHealthReport(getServiceName(), "HEALTHY", "All circuit breakers closed.");
    }

    // 4️⃣ Private Helper Methods (Java 9+): Encourages DRY code within default methods
    private void logRetryAttempt(String operation, int attempt, int total) {
        System.out.printf("      🔄 [%s] Executing retry attempt %d/%d for operation: %s%n",
                getServiceName(), attempt, total, operation);
    }

    private void recordMetrics(String operation, String outcome, long durationMs) {
        String event = formatTelemetryEvent(getServiceName(), outcome, durationMs);
        System.out.println("      📊 Metrics: " + event);
    }

    private String buildFormattedHealthReport(String name, String status, String details) {
        return String.format("ServiceReport{name='%s', status='%s', details='%s', version='%s'}",
                name, status, details, PROTOCOL_VERSION);
    }
}
