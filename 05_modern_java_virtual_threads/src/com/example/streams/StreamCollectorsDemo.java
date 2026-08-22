package com.example.streams;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Section 5.2.2: Collectors API & Aggregation Mechanics.
 * 
 * Demonstrates:
 * - Standard Collectors: toList, toSet, toMap, toUnmodifiableList.
 * - Grouping: groupingBy (single & multi-level, downstream collectors).
 * - Partitioning: partitioningBy (boolean key maps).
 * - String Formatting: Collectors.joining (with delimiters, prefix, suffix).
 * - Advanced Collectors: Collectors.teeing (JDK 12+ single-pass dual collector).
 * - Custom Collector implementation using Collector.of().
 */
public class StreamCollectorsDemo {

    public record ServiceMetric(
        String serviceName, 
        String region, 
        int statusCode, 
        double latencyMs, 
        long payloadSizeBytes
    ) {}

    // Custom Accumulator Record for custom Collector demo
    public record LatencySummary(double min, double max, double sum, long count) {
        public double average() {
            return count == 0 ? 0.0 : sum / count;
        }

        @Override
        public String toString() {
            return String.format("Summary[min=%.2f, max=%.2f, avg=%.2f, count=%d]", min, max, average(), count);
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.2.2 STREAM COLLECTORS: groupingBy, partitioningBy, joining & teeing");
        System.out.println("------------------------------------------------------------------------");

        List<ServiceMetric> metrics = List.of(
            new ServiceMetric("auth-service", "us-east-1", 200, 45.2, 1024),
            new ServiceMetric("auth-service", "us-east-1", 500, 320.0, 512),
            new ServiceMetric("payment-service", "us-east-1", 200, 150.8, 4096),
            new ServiceMetric("payment-service", "eu-west-1", 200, 180.5, 4096),
            new ServiceMetric("user-service", "us-east-1", 200, 60.1, 2048),
            new ServiceMetric("user-service", "eu-west-1", 503, 1250.0, 256),
            new ServiceMetric("auth-service", "eu-west-1", 200, 55.0, 1024)
        );

        // ==========================================
        // 1. Basic Collectors (toList, toSet, toMap)
        // ==========================================
        System.out.println("\n--- 1. Basic Collectors (toSet, toMap) ---");

        Set<String> uniqueRegions = metrics.stream()
            .map(ServiceMetric::region)
            .collect(Collectors.toSet());

        // Map serviceName -> average latency (handling key collisions using merge function)
        Map<String, Double> serviceAvgLatencyMap = metrics.stream()
            .collect(Collectors.toMap(
                ServiceMetric::serviceName,
                ServiceMetric::latencyMs,
                (existing, replacement) -> (existing + replacement) / 2.0
            ));

        System.out.println("Unique Regions Set         : " + uniqueRegions);
        System.out.println("Service Avg Latency Map    : " + serviceAvgLatencyMap);

        // ==========================================
        // 2. groupingBy (Single, Multi-Level & Downstream Collectors)
        // ==========================================
        System.out.println("\n--- 2. groupingBy (Downstream Collectors) ---");

        // Group 1: Service Name -> List of Metrics
        Map<String, List<ServiceMetric>> metricsByService = metrics.stream()
            .collect(Collectors.groupingBy(ServiceMetric::serviceName));

        System.out.println("Service Group Count        : " + metricsByService.keySet());

        // Group 2: Downstream counting (Service Name -> Count of requests)
        Map<String, Long> requestCountByService = metrics.stream()
            .collect(Collectors.groupingBy(ServiceMetric::serviceName, Collectors.counting()));

        System.out.println("Request Count by Service   : " + requestCountByService);

        // Group 3: Downstream averagingDouble (Service Name -> Average Latency)
        Map<String, Double> avgLatencyByService = metrics.stream()
            .collect(Collectors.groupingBy(
                ServiceMetric::serviceName,
                Collectors.averagingDouble(ServiceMetric::latencyMs)
            ));

        System.out.println("Avg Latency by Service     : " + avgLatencyByService);

        // Group 4: Multi-Level groupingBy (Region -> (Status Code -> List of Services))
        Map<String, Map<Integer, List<String>>> regionStatusMap = metrics.stream()
            .collect(Collectors.groupingBy(
                ServiceMetric::region,
                Collectors.groupingBy(
                    ServiceMetric::statusCode,
                    Collectors.mapping(ServiceMetric::serviceName, Collectors.toList())
                )
            ));

        System.out.println("Multi-Level Region Status  : " + regionStatusMap);

        // ==========================================
        // 3. partitioningBy (Boolean Categorization)
        // ==========================================
        System.out.println("\n--- 3. partitioningBy (Errors vs Successes) ---");

        Map<Boolean, List<ServiceMetric>> errorPartitionMap = metrics.stream()
            .collect(Collectors.partitioningBy(m -> m.statusCode() >= 400));

        System.out.println("Successful Requests Count  : " + errorPartitionMap.get(false).size());
        System.out.println("Failed Requests Count      : " + errorPartitionMap.get(true).size());

        // ==========================================
        // 4. Collectors.joining
        // ==========================================
        System.out.println("\n--- 4. Collectors.joining ---");

        String formattedServices = metrics.stream()
            .map(ServiceMetric::serviceName)
            .distinct()
            .collect(Collectors.joining(" | ", "[SERVICES: ", "]"));

        System.out.println("Formatted Services String  : " + formattedServices);

        // ==========================================
        // 5. Collectors.teeing (JDK 12+ Dual Collector)
        // ==========================================
        System.out.println("\n--- 5. Collectors.teeing (Single-pass Dual Aggregation) ---");

        // Single pass over stream: Calculate both Average Latency AND Highest Latency Metric simultaneously!
        record LatencyAnalysis(double averageLatency, Optional<ServiceMetric> maxLatencyMetric) {}

        LatencyAnalysis analysis = metrics.stream()
            .collect(Collectors.teeing(
                Collectors.averagingDouble(ServiceMetric::latencyMs),
                Collectors.maxBy(Comparator.comparingDouble(ServiceMetric::latencyMs)),
                LatencyAnalysis::new
            ));

        System.out.printf("Teeing Result -> Overall Avg: %.2f ms, Max Latency Service: %s (%.2f ms)\n",
            analysis.averageLatency(),
            analysis.maxLatencyMetric().map(ServiceMetric::serviceName).orElse("N/A"),
            analysis.maxLatencyMetric().map(ServiceMetric::latencyMs).orElse(0.0)
        );

        // ==========================================
        // 6. Custom Collector using Collector.of()
        // ==========================================
        System.out.println("\n--- 6. Custom Collector (Collector.of) ---");

        Collector<ServiceMetric, double[], LatencySummary> customLatencySummaryCollector = Collector.of(
            () -> new double[]{Double.MAX_VALUE, Double.MIN_VALUE, 0.0, 0.0}, // Supplier: [min, max, sum, count]
            (acc, m) -> { // Accumulator
                acc[0] = Math.min(acc[0], m.latencyMs());
                acc[1] = Math.max(acc[1], m.latencyMs());
                acc[2] += m.latencyMs();
                acc[3] += 1.0;
            },
            (acc1, acc2) -> { // Combiner (for parallel streams)
                acc1[0] = Math.min(acc1[0], acc2[0]);
                acc1[1] = Math.max(acc1[1], acc2[1]);
                acc1[2] += acc2[2];
                acc1[3] += acc2[3];
                return acc1;
            },
            acc -> new LatencySummary(acc[0], acc[1], acc[2], (long) acc[3]) // Finisher
        );

        LatencySummary customSummary = metrics.stream().collect(customLatencySummaryCollector);
        System.out.println("Custom Collector Summary   : " + customSummary);

        System.out.println("\n💡 SRE Insight: Using downstream collectors (e.g. groupingBy with averagingDouble) avoids creating intermediate list objects");
        System.out.println("   on the heap, vastly reducing GC pauses when analyzing massive log or metric telemetry streams.");
    }
}
