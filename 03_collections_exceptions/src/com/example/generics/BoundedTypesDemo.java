package com.example.generics;

import java.util.Arrays;
import java.util.List;

/**
 * Section 3.3.2: Bounded Type Parameters.
 * 
 * Demonstrates:
 * - Upper Bounded Type (<T extends Comparable<T>>).
 * - Multiple Bounds (<T extends Number & Comparable<T>>).
 * - Syntax ordering rules (Class must precede interfaces).
 * - Implementing algorithms relying on interface/class contract methods.
 */
public class BoundedTypesDemo {

    /**
     * Domain model representing a service metric that implements Comparable<Metric>.
     */
    public static class Metric implements Comparable<Metric> {
        private final String name;
        private final double latencyMs;

        public Metric(String name, double latencyMs) {
            this.name = name;
            this.latencyMs = latencyMs;
        }

        public String getName() { return name; }
        public double getLatencyMs() { return latencyMs; }

        @Override
        public int compareTo(Metric other) {
            return Double.compare(this.latencyMs, other.latencyMs);
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f ms)", name, latencyMs);
        }
    }

    /**
     * Generic method with Upper-Bounded Type (<T extends Comparable<T>>).
     * Finds maximum element in a list based on natural ordering.
     */
    public static <T extends Comparable<T>> T findMax(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            T item = list.get(i);
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    /**
     * Multiple Bounds Class (<T extends Number & Comparable<T>>).
     * 
     * Rule: Class bound (Number) MUST come first, followed by interface bounds (& Comparable<T>).
     * Syntax error occurs if order is reversed (<T extends Comparable<T> & Number>).
     */
    public static class NumericStats<T extends Number & Comparable<T>> {
        private final List<T> numbers;

        public NumericStats(List<T> numbers) {
            if (numbers == null || numbers.isEmpty()) {
                throw new IllegalArgumentException("Numbers list cannot be null or empty");
            }
            this.numbers = numbers;
        }

        /**
         * Calculates double value sum using methods from Number class.
         */
        public double calculateSum() {
            double sum = 0.0;
            for (T num : numbers) {
                sum += num.doubleValue(); // doubleValue() comes from Number class bound
            }
            return sum;
        }

        /**
         * Calculates average value.
         */
        public double calculateAverage() {
            return calculateSum() / numbers.size();
        }

        /**
         * Finds maximum numeric value using Comparable interface bound.
         */
        public T findMaximum() {
            T max = numbers.get(0);
            for (T num : numbers) {
                if (num.compareTo(max) > 0) { // compareTo() comes from Comparable bound
                    max = num;
                }
            }
            return max;
        }

        /**
         * Counts elements greater than a threshold.
         */
        public int countGreaterThan(T threshold) {
            int count = 0;
            for (T num : numbers) {
                if (num.compareTo(threshold) > 0) {
                    count++;
                }
            }
            return count;
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.3.2 BOUNDED TYPES: Single (<T extends Comparable<T>>) & Multiple Bounds");
        System.out.println("------------------------------------------------------------------------");

        // 1. Single Upper Bound (<T extends Comparable<T>>)
        System.out.println("\n--- 1. Single Upper-Bound (<T extends Comparable<T>>) ---");
        List<Metric> metrics = List.of(
                new Metric("HTTP GET /api/v1/users", 45.2),
                new Metric("POST /api/v1/orders", 320.5),
                new Metric("GET /health", 3.1)
        );

        Metric maxLatencyMetric = findMax(metrics);
        System.out.println("All Metrics          : " + metrics);
        System.out.println("Max Latency Metric   : " + maxLatencyMetric);

        List<String> services = Arrays.asList("auth-service", "user-service", "payment-service", "billing-service");
        String maxAlphabeticalService = findMax(services);
        System.out.println("Max Service (Alpha)  : " + maxAlphabeticalService);

        // 2. Multiple Bounds (<T extends Number & Comparable<T>>)
        System.out.println("\n--- 2. Multiple Bounds (<T extends Number & Comparable<T>>) ---");
        List<Integer> cpuUsages = List.of(45, 82, 91, 34, 67, 98, 55);
        NumericStats<Integer> integerStats = new NumericStats<>(cpuUsages);

        System.out.println("CPU Usages (%)       : " + cpuUsages);
        System.out.println("CPU Total Sum        : " + integerStats.calculateSum());
        System.out.println("CPU Average          : " + String.format("%.2f%%", integerStats.calculateAverage()));
        System.out.println("Peak CPU Usage       : " + integerStats.findMaximum() + "%");
        System.out.println("Spikes (>80% CPU)    : " + integerStats.countGreaterThan(80));

        List<Double> responseTimesMs = List.of(12.5, 4.2, 105.8, 56.4, 210.0);
        NumericStats<Double> doubleStats = new NumericStats<>(responseTimesMs);

        System.out.println("\nResponse Times (ms)  : " + responseTimesMs);
        System.out.println("Average Response Time: " + String.format("%.2f ms", doubleStats.calculateAverage()));
        System.out.println("Max Response Time    : " + doubleStats.findMaximum() + " ms");

        System.out.println("\n💡 SRE Constraint Rule: Multiple type bounds syntax MUST place class bound FIRST:");
        System.out.println("   Correct:   <T extends Number & Comparable<T>>");
        System.out.println("   Incorrect: <T extends Comparable<T> & Number> -> Compile Error: 'interface expected here'");
    }
}
