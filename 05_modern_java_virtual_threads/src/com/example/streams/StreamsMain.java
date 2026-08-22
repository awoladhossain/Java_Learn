package com.example.streams;

/**
 * Main Runner Class for Phase 5.2: Java Streams API Deep-Dive.
 * 
 * Executes comprehensive demonstrations covering:
 * - 5.2.1 Stream Life Cycle & Operation Pipeline (filter, map, flatMap, sorted, distinct, reduce, lazy evaluation).
 * - 5.2.2 Collectors API (groupingBy, partitioningBy, joining, teeing, custom Collector.of).
 * - 5.2.3 Primitive Streams (IntStream, LongStream, DoubleStream, SummaryStatistics, Memory/GC optimization).
 * - 5.2.4 Parallel Streams (ForkJoinPool.commonPool mechanics, thread starvation traps, custom pool isolation, N*Q rule).
 */
public class StreamsMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("⚡ PHASE 5.2: JAVA STREAMS API DEEP-DIVE DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Stream Life Cycle & Operations
        StreamLifecycleDemo.runDemo();

        // 2. Collectors API & Aggregations
        StreamCollectorsDemo.runDemo();

        // 3. Primitive Streams & High Performance
        PrimitiveStreamsDemo.runDemo();

        // 4. Parallel Streams & Concurrency Hazards
        ParallelStreamsDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 5.2 JAVA STREAMS API EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
