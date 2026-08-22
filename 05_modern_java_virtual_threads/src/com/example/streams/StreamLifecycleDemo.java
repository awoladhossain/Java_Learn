package com.example.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Section 5.2.1: Stream Life Cycle & Operation Pipeline.
 * 
 * Demonstrates:
 * - Stream Life Cycle: Source -> Intermediate Operations -> Terminal Operations.
 * - Intermediate Operations: filter, map, flatMap, sorted, distinct, peek, limit, skip.
 * - Lazy Evaluation & Short-Circuiting behavior.
 * - Terminal Operations: collect, reduce, findFirst, findAny, anyMatch, allMatch, noneMatch.
 */
public class StreamLifecycleDemo {

    // Domain record representing a web service access log entry
    public record AccessLog(
        String requestId, 
        String endpoint, 
        int statusCode, 
        long responseTimeMs, 
        List<String> tags
    ) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.2.1 STREAM LIFE CYCLE: Intermediate vs Terminal Operations");
        System.out.println("------------------------------------------------------------------------");

        List<AccessLog> logs = List.of(
            new AccessLog("req-001", "/api/v1/checkout", 200, 120, List.of("payment", "v1")),
            new AccessLog("req-002", "/api/v1/users", 500, 450, List.of("user", "error")),
            new AccessLog("req-003", "/api/v1/checkout", 200, 85, List.of("payment", "v1")),
            new AccessLog("req-004", "/api/v1/auth", 401, 30, List.of("auth", "security")),
            new AccessLog("req-005", "/api/v1/users", 200, 210, List.of("user", "v1")),
            new AccessLog("req-006", "/api/v1/checkout", 503, 1200, List.of("payment", "error", "critical"))
        );

        // ==========================================
        // 1. Stream Sources & Creation Variants
        // ==========================================
        System.out.println("\n--- 1. Stream Sources & Creation ---");

        Stream<String> staticStream = Stream.of("log-alpha", "log-beta", "log-gamma");
        Stream<String> arrayStream = Arrays.stream(new String[]{"node-1", "node-2"});
        Stream<Integer> generatedStream = Stream.iterate(1, n -> n * 2).limit(5); // 1, 2, 4, 8, 16

        System.out.println("Static Stream count    : " + staticStream.count());
        System.out.println("Array Stream count     : " + arrayStream.count());
        System.out.println("Generated Stream items : " + generatedStream.toList());

        // ==========================================
        // 2. Intermediate Operations & Lazy Evaluation
        // ==========================================
        System.out.println("\n--- 2. Intermediate Ops (filter, map, flatMap, sorted, distinct, peek) ---");

        AtomicInteger evaluationCounter = new AtomicInteger(0);

        System.out.println("Building Stream pipeline (No terminal operation called yet)...");
        Stream<String> lazyPipeline = logs.stream()
            .peek(l -> evaluationCounter.incrementAndGet()) // Debug step to trace execution
            .filter(l -> l.statusCode() >= 400)             // Filter errors
            .map(AccessLog::endpoint)                        // Map log to endpoint string
            .distinct();                                     // Deduplicate endpoints

        System.out.println("Evaluations triggered prior to terminal operation: " + evaluationCounter.get() + " (Lazy evaluation!)");

        System.out.println("Triggering Terminal Operation (.toList())...");
        List<String> errorEndpoints = lazyPipeline.toList();
        System.out.println("Evaluations triggered AFTER terminal operation: " + evaluationCounter.get());
        System.out.println("Unique Error Endpoints: " + errorEndpoints);

        // flatMap Example: Flattening nested lists of tags into a single stream
        System.out.println("\n--- FlatMap Demonstration ---");
        List<String> uniqueTags = logs.stream()
            .flatMap(l -> l.tags().stream()) // Flatten List<String> inside AccessLog to Stream<String>
            .distinct()
            .sorted()
            .toList();
        System.out.println("All Unique Tags (flattened): " + uniqueTags);

        // ==========================================
        // 3. Short-Circuiting Operations
        // ==========================================
        System.out.println("\n--- 3. Short-Circuiting Operations (findFirst, anyMatch, limit) ---");

        // anyMatch halts stream evaluation as soon as first match is found
        boolean hasCriticalError = logs.stream()
            .peek(l -> System.out.println("   Checking log for 5xx: " + l.requestId()))
            .anyMatch(l -> l.statusCode() >= 500);

        System.out.println("Has 5xx Error? " + hasCriticalError + " (Notice processing stopped at req-002)");

        Optional<AccessLog> slowestLog = logs.stream()
            .filter(l -> l.responseTimeMs() > 100)
            .findFirst();

        slowestLog.ifPresent(l -> 
            System.out.println("First log with latency > 100ms: " + l.requestId() + " (" + l.responseTimeMs() + "ms)"));

        // ==========================================
        // 4. Reduction Operations (reduce)
        // ==========================================
        System.out.println("\n--- 4. Reduction Operations (reduce) ---");

        // Sum total response times using reduce
        long totalResponseTime = logs.stream()
            .map(AccessLog::responseTimeMs)
            .reduce(0L, Long::sum);

        // Find max latency using reduce
        Optional<Long> maxLatency = logs.stream()
            .map(AccessLog::responseTimeMs)
            .reduce(Long::max);

        System.out.println("Total Response Time : " + totalResponseTime + " ms");
        System.out.println("Max Response Time   : " + maxLatency.orElse(0L) + " ms");

        System.out.println("\n💡 SRE Insight: Streams process elements vertically (one item at a time through all stages).");
        System.out.println("   Short-circuiting terminal operations like findFirst() or anyMatch() avoid scanning millions of unnecessary log records!");
    }
}
