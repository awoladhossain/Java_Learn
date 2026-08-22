package com.example.functional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Section 5.1.2: Built-in Functional Interfaces.
 * 
 * Demonstrates standard java.util.function interfaces:
 * - Supplier<T>: Lazy generation / fallback provider.
 * - Consumer<T>: Side-effect execution & pipeline chaining (andThen).
 * - Function<T, R>: Transformations, composition (andThen, compose, identity).
 * - Predicate<T>: Boolean conditions & logical operators (and, or, negate, isEqual, not).
 * - UnaryOperator<T> & BinaryOperator<T>: Specializations for same-type transformations & aggregations.
 * - Bi-variants: BiFunction<T, U, R>, BiPredicate<T, U>, BiConsumer<T, U>.
 */
public class BuiltInFunctionalInterfacesDemo {

    // Domain models for real-world SRE demonstration
    public record HttpRequest(String id, String path, String clientIp, Map<String, String> headers, String body) {}
    public record HttpResponse(int statusCode, String body, long latencyMs) {}
    public record SecurityContext(String clientIp, boolean isAuthenticated, boolean isRateLimited) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.1.2 BUILT-IN FUNCTIONAL INTERFACES (java.util.function)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Supplier<T> (Lazy Evaluation & Fallbacks)
        // ==========================================
        System.out.println("\n--- 1. Supplier<T> (Lazy Evaluation & Fallback Factory) ---");
        
        // Simulating heavy database lookup or default config generator
        Supplier<Map<String, String>> defaultConfigSupplier = () -> {
            System.out.println("   [Supplier] Generating default configuration fallback...");
            Map<String, String> config = new HashMap<>();
            config.put("timeoutMs", "5000");
            config.put("maxConnections", "100");
            config.put("environment", "production-fallback");
            return config;
        };

        // Standard use case: getOrDefault pattern without executing expensive code upfront
        Map<String, String> activeConfig = fetchConfigOrFallback(null, defaultConfigSupplier);
        System.out.println("Active Config Result: " + activeConfig);

        // ==========================================
        // 2. Consumer<T> & Consumer Chaining (andThen)
        // ==========================================
        System.out.println("\n--- 2. Consumer<T> & Side-Effect Pipeline (andThen) ---");

        List<String> auditLogs = new ArrayList<>();

        Consumer<HttpResponse> consoleLogger = resp -> 
            System.out.printf("   [LOG] HTTP Response %d (Latency: %dms)\n", resp.statusCode(), resp.latencyMs());

        Consumer<HttpResponse> auditRecorder = resp -> 
            auditLogs.add(String.format("AUDIT: status=%d, latency=%d", resp.statusCode(), resp.latencyMs()));

        Consumer<HttpResponse> metricEmitter = resp -> {
            if (resp.statusCode() >= 500) {
                System.out.printf("   [METRIC] Counter incremented: http_errors_5xx_total{status=\"%d\"}\n", resp.statusCode());
            }
        };

        // Chaining multiple consumers using andThen
        Consumer<HttpResponse> responsePipeline = consoleLogger
                .andThen(auditRecorder)
                .andThen(metricEmitter);

        System.out.println("Executing Response Pipeline for 200 OK:");
        responsePipeline.accept(new HttpResponse(200, "{\"status\":\"ok\"}", 14));

        System.out.println("Executing Response Pipeline for 503 Service Unavailable:");
        responsePipeline.accept(new HttpResponse(503, "{\"error\":\"Database Timeout\"}", 2500));

        System.out.println("Audit Logs Collected: " + auditLogs);

        // ==========================================
        // 3. Function<T, R> (Transformation & Composition)
        // ==========================================
        System.out.println("\n--- 3. Function<T, R> (Transformation, compose, andThen, identity) ---");

        // Function 1: Extract body length
        Function<HttpRequest, String> extractBody = HttpRequest::body;

        // Function 2: Sanitize string (trim & uppercase)
        Function<String, String> sanitize = str -> str == null ? "" : str.trim().toUpperCase();

        // Function 3: Wrap in JSON payload envelope
        Function<String, String> wrapJson = str -> String.format("{\"cleanedPayload\":\"%s\"}", str);

        // Function Composition: andThen (extractBody -> sanitize -> wrapJson)
        Function<HttpRequest, String> pipeline = extractBody.andThen(sanitize).andThen(wrapJson);

        HttpRequest req = new HttpRequest("req-101", "/api/v1/users", "10.0.0.5", Map.of(), "  john.doe@example.com  ");
        System.out.println("Original HttpRequest Body: '" + req.body() + "'");
        System.out.println("Transformed Json Payload : " + pipeline.apply(req));

        // Difference between compose and andThen:
        // f.compose(g) executes g FIRST, then f.
        // f.andThen(g) executes f FIRST, then g.
        Function<Integer, Integer> multiplyBy2 = x -> x * 2;
        Function<Integer, Integer> add10 = x -> x + 10;

        System.out.println("(5 * 2) + 10 [using andThen]: " + multiplyBy2.andThen(add10).apply(5)); // (5*2)+10 = 20
        System.out.println("(5 + 10) * 2 [using compose]: " + multiplyBy2.compose(add10).apply(5)); // (5+10)*2 = 30

        // Function.identity() -> returns input unchanged
        Function<String, String> identityFunc = Function.identity();
        System.out.println("Function.identity(): " + identityFunc.apply("UNCHANGED_INPUT"));

        // ==========================================
        // 4. Predicate<T> (Boolean Logic & Composition)
        // ==========================================
        System.out.println("\n--- 4. Predicate<T> (and, or, negate, isEqual, not) ---");

        Predicate<SecurityContext> isAuth = SecurityContext::isAuthenticated;
        Predicate<SecurityContext> isNotRateLimited = ctx -> !ctx.isRateLimited();
        Predicate<SecurityContext> isInternalIp = ctx -> ctx.clientIp().startsWith("10.") || ctx.clientIp().startsWith("192.168.");

        // Combining Predicates: Allowed if (IsInternal AND Auth) OR (Not RateLimited AND Auth)
        Predicate<SecurityContext> accessGrantedPredicate = isAuth.and(isInternalIp.or(isNotRateLimited));
        Predicate<SecurityContext> accessDeniedPredicate = Predicate.not(accessGrantedPredicate); // JDK 11 Predicate.not()

        SecurityContext ctx1 = new SecurityContext("10.0.0.12", true, false);  // Internal, auth, not limited -> ALLOW
        SecurityContext ctx2 = new SecurityContext("203.0.113.5", false, true); // External, unauth, limited -> DENY

        System.out.println("Security Context 1 Granted? " + accessGrantedPredicate.test(ctx1));
        System.out.println("Security Context 2 Denied?  " + accessDeniedPredicate.test(ctx2));

        // Predicate.isEqual
        Predicate<String> isAdminRole = Predicate.isEqual("ROLE_ADMIN");
        System.out.println("Is 'ROLE_ADMIN' admin? " + isAdminRole.test("ROLE_ADMIN"));
        System.out.println("Is 'ROLE_USER' admin?  " + isAdminRole.test("ROLE_USER"));

        // ==========================================
        // 5. UnaryOperator<T>, BinaryOperator<T> & Bi-Variants
        // ==========================================
        System.out.println("\n--- 5. UnaryOperator<T>, BinaryOperator<T>, and Bi-Variants ---");

        // UnaryOperator<T> is Function<T, T>
        UnaryOperator<String> headerSanitizer = header -> header.replaceAll("[^a-zA-Z0-9-]", "_").toLowerCase();
        System.out.println("Sanitizing Header 'X-Forwarded-For!': " + headerSanitizer.apply("X-Forwarded-For!"));

        // BinaryOperator<T> is BiFunction<T, T, T>
        BinaryOperator<Double> latencyAccumulator = Double::sum;
        Double totalLatency = latencyAccumulator.apply(12.5, 48.3);
        System.out.println("Accumulated Latencies: " + totalLatency + " ms");

        // BiPredicate<T, U>
        BiPredicate<String, Integer> portValidator = (scheme, port) -> 
            ("https".equalsIgnoreCase(scheme) && port == 443) || ("http".equalsIgnoreCase(scheme) && port == 80);
        System.out.println("Is https:443 valid? " + portValidator.test("https", 443));
        System.out.println("Is http:443 valid?  " + portValidator.test("http", 443));

        // BiConsumer<K, V>
        BiConsumer<String, String> printHeader = (k, v) -> System.out.println("   Header -> " + k + ": " + v);
        Map.of("Authorization", "Bearer token-xyz", "Content-Type", "application/json").forEach(printHeader);
    }

    private static <T> T fetchConfigOrFallback(T existingConfig, Supplier<T> fallbackSupplier) {
        if (existingConfig != null) {
            return existingConfig;
        }
        return Objects.requireNonNull(fallbackSupplier.get(), "Fallback config cannot be null");
    }
}
