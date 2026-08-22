package com.example.functional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Section 5.1.3: Custom @FunctionalInterface Definitions & Exception Handling.
 * 
 * Demonstrates:
 * - Creating custom SAM (Single Abstract Method) interfaces annotated with @FunctionalInterface.
 * - Adding default & static methods inside functional interfaces for composition & utility.
 * - Solving the Java Checked Exception problem in Lambdas via ThrowingFunction<T, R, E>.
 * - Production-ready telemetry filter & event handler custom interfaces.
 */
public class CustomFunctionalInterfaceDemo {

    /**
     * 1. Custom Functional Interface for Rate Limiting / Metric Filtering.
     * Single Abstract Method: boolean matches(T metric);
     */
    @FunctionalInterface
    public interface TelemetryFilter<T> {
        
        // Single Abstract Method (SAM)
        boolean matches(T telemetryItem);

        // Default Method 1: Logical AND composition
        default TelemetryFilter<T> and(TelemetryFilter<T> other) {
            Objects.requireNonNull(other);
            return item -> this.matches(item) && other.matches(item);
        }

        // Default Method 2: Logical OR composition
        default TelemetryFilter<T> or(TelemetryFilter<T> other) {
            Objects.requireNonNull(other);
            return item -> this.matches(item) || other.matches(item);
        }

        // Default Method 3: Logical NOT negation
        default TelemetryFilter<T> negate() {
            return item -> !this.matches(item);
        }

        // Static Factory Method 1: Always matching filter
        static <T> TelemetryFilter<T> matchAll() {
            return item -> true;
        }

        // Static Factory Method 2: Matching none
        static <T> TelemetryFilter<T> matchNone() {
            return item -> false;
        }
    }

    /**
     * 2. Custom Functional Interface for Event Pipeline Execution.
     */
    @FunctionalInterface
    public interface EventProcessor<E> {
        
        void process(E event);

        default EventProcessor<E> andThen(EventProcessor<E> after) {
            Objects.requireNonNull(after);
            return event -> {
                this.process(event);
                after.process(event);
            };
        }
    }

    /**
     * 3. Functional Interface pattern for cleanly handling Checked Exceptions in Lambdas.
     * Standard Function<T, R> cannot throw checked exceptions (e.g. IOException, SQLException).
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        
        R apply(T target) throws E;

        /**
         * Converts a ThrowingFunction into a standard java.util.function.Function
         * by wrapping checked exceptions in an unchecked RuntimeException.
         */
        static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R, ?> throwingFunction) {
            return target -> {
                try {
                    return throwingFunction.apply(target);
                } catch (Exception e) {
                    if (e instanceof RuntimeException re) {
                        throw re;
                    }
                    throw new RuntimeException("Wrapped checked exception during lambda execution", e);
                }
            };
        }
    }

    // Record for demonstration
    public record SystemEvent(String eventId, String type, int severityLevel) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.1.3 CUSTOM @FunctionalInterface & CHECKED EXCEPTION HANDLING");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Custom TelemetryFilter with Default & Static Methods
        // ==========================================
        System.out.println("\n--- 1. Custom TelemetryFilter (SAM + default & static methods) ---");

        SystemEvent event1 = new SystemEvent("evt-001", "DISK_FULL", 9);
        SystemEvent event2 = new SystemEvent("evt-002", "NETWORK_LATENCY", 4);
        SystemEvent event3 = new SystemEvent("evt-003", "AUTH_FAILURE", 8);

        TelemetryFilter<SystemEvent> isCritical = event -> event.severityLevel() >= 8;
        TelemetryFilter<SystemEvent> isDiskEvent = event -> "DISK_FULL".equalsIgnoreCase(event.type());

        // Composing filters using default methods
        TelemetryFilter<SystemEvent> criticalDiskFilter = isCritical.and(isDiskEvent);
        TelemetryFilter<SystemEvent> nonCriticalFilter = isCritical.negate();

        System.out.println("Is Event 1 (DISK_FULL, sev=9) Critical Disk Event? " + criticalDiskFilter.matches(event1));
        System.out.println("Is Event 3 (AUTH_FAILURE, sev=8) Critical Disk Event? " + criticalDiskFilter.matches(event3));
        System.out.println("Is Event 2 (NETWORK, sev=4) Non-Critical? " + nonCriticalFilter.matches(event2));

        // Static factory method usage
        TelemetryFilter<SystemEvent> allFilter = TelemetryFilter.matchAll();
        System.out.println("MatchAll filter on Event 2: " + allFilter.matches(event2));

        // ==========================================
        // 2. Custom EventProcessor Chain
        // ==========================================
        System.out.println("\n--- 2. Custom EventProcessor Chain (andThen composition) ---");

        List<String> publishedEvents = new ArrayList<>();

        EventProcessor<SystemEvent> validator = evt -> {
            if (evt.eventId() == null || evt.eventId().isBlank()) {
                throw new IllegalArgumentException("Invalid Event ID");
            }
        };

        EventProcessor<SystemEvent> publisher = evt -> 
            publishedEvents.add("KAFKA_PUBLISH -> " + evt.eventId() + " [" + evt.type() + "]");

        EventProcessor<SystemEvent> metricsCounter = evt -> 
            System.out.println("   [STATSD] Counter incremented for event: " + evt.type());

        // Chaining using custom default method
        EventProcessor<SystemEvent> fullPipeline = validator.andThen(publisher).andThen(metricsCounter);

        System.out.println("Running full custom EventProcessor pipeline:");
        fullPipeline.process(event1);
        fullPipeline.process(event3);

        System.out.println("Published Events: " + publishedEvents);

        // ==========================================
        // 3. Checked Exception Handling in Lambdas (ThrowingFunction)
        // ==========================================
        System.out.println("\n--- 3. Checked Exception Handling via ThrowingFunction Wrapper ---");

        List<String> rawUris = List.of(
            "https://api.service.internal/v1/health",
            "https://db.service.internal/v1/metrics",
            "ht tp://invalid uri with spaces" // Will cause URI syntax error (checked URIException / IllegalArgumentException)
        );

        // Simulated function that throws a checked exception (IOException)
        ThrowingFunction<String, URI, Exception> uriParser = rawUri -> {
            if (rawUri.contains(" ")) {
                throw new IOException("Failed to parse URI due to illegal characters: " + rawUri);
            }
            return URI.create(rawUri);
        };

        // Convert ThrowingFunction into standard Function using unchecked static helper
        Function<String, URI> safeUriParser = ThrowingFunction.unchecked(uriParser);

        for (String uriStr : rawUris) {
            try {
                URI parsed = safeUriParser.apply(uriStr);
                System.out.println("   Successfully parsed URI: " + parsed);
            } catch (RuntimeException re) {
                System.out.println("   Cleanly caught wrapped exception: " + re.getMessage() + 
                                   " | Cause: " + re.getCause());
            }
        }

        System.out.println("\n💡 SRE Insight: Wrapping checked exceptions inside custom functional adapters keeps Stream and Lambda pipelines clean,");
        System.out.println("   preventing boilerplate try-catch blocks while ensuring exception causes are preserved for observability.");
    }
}
