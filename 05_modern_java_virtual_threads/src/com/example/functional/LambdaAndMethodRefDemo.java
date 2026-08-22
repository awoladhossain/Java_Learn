package com.example.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Section 5.1.1: Lambda Expressions & Method References.
 * 
 * Demonstrates:
 * - Lambda Expression variants (expression vs block, parameter type inference).
 * - Variable Capture & Effectively Final rule semantics in closing scope.
 * - 4 Types of Method References:
 *   1. Static Method Reference (Class::staticMethod)
 *   2. Bound Instance Method Reference (instance::instanceMethod)
 *   3. Unbound Instance Method Reference (Class::instanceMethod)
 *   4. Constructor Reference (Class::new / Array[]::new)
 * - SRE & Performance Insights regarding lambda allocations and invokedynamic.
 */
public class LambdaAndMethodRefDemo {

    // Record representing a server metric payload
    public record ServerMetric(String host, double cpuUsage, double memoryUsage) {
        public static boolean isOverloaded(ServerMetric metric) {
            return metric.cpuUsage() > 80.0 || metric.memoryUsage() > 90.0;
        }

        public String formatReport() {
            return String.format("[%s] CPU: %.1f%%, MEM: %.1f%%", host, cpuUsage, memoryUsage);
        }
    }

    // Logger component for bound instance reference demo
    public static class MetricLogger {
        private final String prefix;

        public MetricLogger(String prefix) {
            this.prefix = prefix;
        }

        public void logMetric(String message) {
            System.out.println(prefix + " " + message);
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.1.1 LAMBDA EXPRESSIONS & METHOD REFERENCES (Class::method)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Lambda Expressions & Scope Capture
        // ==========================================
        System.out.println("\n--- 1. Lambda Expression Variations & Variable Capture ---");

        // Expression Lambda with type inference
        Function<Double, Double> celsiusToFahrenheit = c -> (c * 9 / 5) + 32;
        System.out.println("Celsius 100°C to Fahrenheit: " + celsiusToFahrenheit.apply(100.0) + "°F");

        // Block Lambda with explicit return and multi-statement logic
        BiFunction<String, Double, String> alertGenerator = (host, cpu) -> {
            if (cpu > 85.0) {
                return String.format("CRITICAL: High CPU on %s (%.2f%%)", host, cpu);
            } else if (cpu > 70.0) {
                return String.format("WARNING: Moderate CPU on %s (%.2f%%)", host, cpu);
            }
            return String.format("INFO: Normal CPU on %s (%.2f%%)", host, cpu);
        };

        System.out.println("Alert Check 1: " + alertGenerator.apply("prod-app-01", 92.5));
        System.out.println("Alert Check 2: " + alertGenerator.apply("prod-app-02", 45.0));

        // Variable Capture (Effectively Final constraint)
        String env = "PRODUCTION"; // Effectively final local variable
        int threshold = 80;        // Effectively final

        Consumer<ServerMetric> envAwareChecker = metric -> {
            // env and threshold are captured from enclosing scope
            if (metric.cpuUsage() > threshold) {
                System.out.printf("[%s ALERT] Host %s exceeded threshold %d%% with usage %.1f%%\n",
                        env, metric.host(), threshold, metric.cpuUsage());
            }
        };

        ServerMetric testMetric = new ServerMetric("db-master-01", 88.4, 65.0);
        envAwareChecker.accept(testMetric);

        // ==========================================
        // 2. Method References (4 Standard Categories)
        // ==========================================
        System.out.println("\n--- 2. Four Types of Method References ---");

        List<ServerMetric> metricsList = List.of(
            new ServerMetric("web-01", 45.0, 50.0),
            new ServerMetric("web-02", 85.5, 60.0),
            new ServerMetric("api-01", 91.0, 95.0)
        );

        // Category A: Static Method Reference (ContainingClass::staticMethod)
        // Lambda equivalent: metric -> ServerMetric.isOverloaded(metric)
        java.util.function.Predicate<ServerMetric> overloadedCheck = ServerMetric::isOverloaded;
        System.out.println("Static Method Ref - Is api-01 overloaded? " + overloadedCheck.test(metricsList.get(2)));

        // Category B: Bound Instance Method Reference (instanceObject::instanceMethod)
        // Bound to specific MetricLogger instance
        MetricLogger logger = new MetricLogger("[SRE-LOG]");
        // Lambda equivalent: msg -> logger.logMetric(msg)
        Consumer<String> boundLogConsumer = logger::logMetric;
        boundLogConsumer.accept("System health audit passed.");

        // Category C: Unbound Instance Method Reference (ContainingClass::instanceMethod)
        // First parameter of functional interface becomes the target instance
        // Lambda equivalent: metric -> metric.formatReport()
        Function<ServerMetric, String> reportFormatter = ServerMetric::formatReport;
        for (ServerMetric m : metricsList) {
            System.out.println("Unbound Method Ref: " + reportFormatter.apply(m));
        }

        // Category D: Constructor Reference (Class::new & Array[]::new)
        // Class Constructor Reference (Supplier / BiFunction)
        // Lambda equivalent: () -> new ArrayList<ServerMetric>()
        Supplier<List<ServerMetric>> listFactory = ArrayList::new;
        List<ServerMetric> mutableList = listFactory.get();
        mutableList.addAll(metricsList);

        // Array Constructor Reference
        // Lambda equivalent: size -> new ServerMetric[size]
        IntFunction<ServerMetric[]> arrayFactory = ServerMetric[]::new;
        ServerMetric[] metricsArray = mutableList.toArray(arrayFactory.apply(0));
        System.out.println("Constructor Ref created array of size " + metricsArray.length + ": " + Arrays.toString(metricsArray));

        // ==========================================
        // 3. SRE Insight & JVM Performance
        // ==========================================
        System.out.println("\n💡 SRE & Performance Insight:");
        System.out.println("   1. Java lambdas use the 'invokedynamic' opcode (JVM dynamic call sites) avoiding boilerplate anonymous class file generation.");
        System.out.println("   2. Non-capturing lambdas & stateless method references (e.g. String::toUpperCase) are converted to singletons by JVM runtime, resulting in ZERO object allocations.");
        System.out.println("   3. Capturing lambdas allocate a fresh closure object on Heap per invocation—avoid capturing local variables inside tight loops!");
    }
}
