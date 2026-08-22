package com.example.modern;

import java.util.List;
import java.util.Optional;

/**
 * Section 5.3.1: Optional<T> API & Monadic Null Safety.
 * 
 * Demonstrates:
 * - Creation: Optional.of(), ofNullable(), empty().
 * - Safe Value Extraction: orElse, orElseGet (lazy evaluation), orElseThrow.
 * - Monadic Transformations: map, flatMap, filter.
 * - Modern Methods: ifPresent, ifPresentOrElse (JDK 9), or (JDK 9), stream (JDK 9).
 * - SRE Anti-patterns & Best Practices (Avoiding NPEs without boilerplate null checks).
 */
public class OptionalApiDemo {

    // Domain Records with nested Optional fields for modeling lookup operations
    public record DatabaseConfig(String host, int port) {}
    public record ApplicationConfig(String appName, Optional<DatabaseConfig> dbConfig) {}
    public record ServiceProfile(String profileId, Optional<ApplicationConfig> appConfig) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.3.1 OPTIONAL<T> API: Eliminating NullPointerExceptions Cleanly");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Optional Creation & Basic Inspection
        // ==========================================
        System.out.println("\n--- 1. Optional Creation & Null Safety ---");

        String nullableHost = null;
        String validHost = "10.0.0.42";

        Optional<String> optEmpty = Optional.ofNullable(nullableHost);
        Optional<String> optValid = Optional.ofNullable(validHost);

        System.out.println("ofNullable(null) isPresent?  " + optEmpty.isPresent());
        System.out.println("ofNullable(null) isEmpty?    " + optEmpty.isEmpty()); // JDK 11 isEmpty()
        System.out.println("ofNullable(valid) getValue?  " + optValid.get());

        // ==========================================
        // 2. Safe Fallback Value Extraction (orElse vs orElseGet vs orElseThrow)
        // ==========================================
        System.out.println("\n--- 2. Fallbacks: orElse vs orElseGet vs orElseThrow ---");

        // Note: orElse() ALWAYS executes the fallback expression even if Optional is present!
        // orElseGet() takes a Supplier and is LAZILY evaluated ONLY if Optional is empty.
        System.out.println("Testing orElse (Eager):");
        String res1 = optValid.orElse(computeExpensiveFallback("orElse-eager"));

        System.out.println("Testing orElseGet (Lazy):");
        String res2 = optValid.orElseGet(() -> computeExpensiveFallback("orElseGet-lazy"));

        System.out.println("Extracted Result: " + res1 + " | " + res2);

        // Custom Exception throwing
        try {
            optEmpty.orElseThrow(() -> new IllegalStateException("Database host configuration is missing!"));
        } catch (IllegalStateException e) {
            System.out.println("Caught expected orElseThrow exception: " + e.getMessage());
        }

        // ==========================================
        // 3. Monadic Pipeline Transformations (map, flatMap, filter)
        // ==========================================
        System.out.println("\n--- 3. Monadic Transformations (map, flatMap, filter) ---");

        DatabaseConfig db = new DatabaseConfig("postgres-primary.internal", 5432);
        ApplicationConfig app = new ApplicationConfig("order-service", Optional.of(db));
        ServiceProfile profileWithDb = new ServiceProfile("prod-profile", Optional.of(app));
        ServiceProfile profileWithoutDb = new ServiceProfile("dev-profile", Optional.of(new ApplicationConfig("dev-service", Optional.empty())));

        // Clean deep navigation through nested Optionals using flatMap (eliminates multi-level null checks!)
        String hostWithDb = extractDatabaseHost(profileWithDb);
        String hostWithoutDb = extractDatabaseHost(profileWithoutDb);

        System.out.println("Extracted DB Host (Profile 1): " + hostWithDb);
        System.out.println("Extracted DB Host (Profile 2): " + hostWithoutDb);

        // Filtering values within Optional
        Optional<DatabaseConfig> postgresOnly = Optional.of(db)
            .filter(d -> d.host().startsWith("postgres"));
        
        System.out.println("Filtered Postgres DB Host: " + postgresOnly.map(DatabaseConfig::host).orElse("NOT_POSTGRES"));

        // ==========================================
        // 4. Modern Optional Features (ifPresentOrElse, or, stream)
        // ==========================================
        System.out.println("\n--- 4. Modern JDK 9+ Methods (ifPresentOrElse, or, stream) ---");

        // ifPresentOrElse (Consumer action, Runnable emptyAction)
        System.out.println("Executing ifPresentOrElse on valid profile:");
        optValid.ifPresentOrElse(
            h -> System.out.println("   [SUCCESS] Connecting to host: " + h),
            () -> System.out.println("   [ERROR] No host configured")
        );

        System.out.println("Executing ifPresentOrElse on empty profile:");
        optEmpty.ifPresentOrElse(
            h -> System.out.println("   [SUCCESS] Connecting to host: " + h),
            () -> System.out.println("   [ERROR] No host configured (Triggered fallback Runnable!)")
        );

        // Optional.or() -> Chaining alternate Optionals if primary is empty
        Optional<String> primaryConfig = Optional.empty();
        Optional<String> secondaryConfig = Optional.empty();
        Optional<String> tertiaryConfig = Optional.of("192.168.1.1");

        Optional<String> resolvedConfig = primaryConfig
            .or(() -> secondaryConfig)
            .or(() -> tertiaryConfig);

        System.out.println("Resolved Chained Fallback Config: " + resolvedConfig.orElse("DEFAULT"));

        // Optional.stream() -> Converting Optionals to Stream (0 or 1 element) for seamless stream pipelines
        List<Optional<String>> optionalList = List.of(
            Optional.of("k8s-pod-1"),
            Optional.empty(),
            Optional.of("k8s-pod-2"),
            Optional.empty()
        );

        List<String> activePods = optionalList.stream()
            .flatMap(Optional::stream) // Unwraps present optionals and discards empty ones in 1 line
            .toList();

        System.out.println("Extracted Active Pods via Optional.stream(): " + activePods);

        // ==========================================
        // 5. Senior SRE Best Practices & Anti-Patterns
        // ==========================================
        System.out.println("\n💡 Senior SRE Best Practices:");
        System.out.println("   1. NEVER call optional.get() without first calling isPresent() (Use orElseGet / orElseThrow instead).");
        System.out.println("   2. Prefer orElseGet(() -> expensiveComputation()) over orElse(expensiveComputation()) to avoid useless performance overhead.");
        System.out.println("   3. Do NOT wrap Collection return types in Optional (Return an empty List/Set/Map instead of Optional<List<T>>).");
        System.out.println("   4. Optional is designed primarily for METHOD RETURN TYPES, not for class fields or constructor parameters.");
    }

    private static String extractDatabaseHost(ServiceProfile profile) {
        return Optional.ofNullable(profile)
            .flatMap(ServiceProfile::appConfig)
            .flatMap(ApplicationConfig::dbConfig)
            .map(DatabaseConfig::host)
            .orElse("DEFAULT_LOCAL_HOST");
    }

    private static String computeExpensiveFallback(String callerTag) {
        System.out.println("   [EXPENSIVE] Executing expensive fallback logic for: " + callerTag);
        return "FALLBACK_HOST_10.0.0.1";
    }
}
