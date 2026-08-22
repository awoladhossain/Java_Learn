package com.example.streams;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Section 5.2.3: Primitive Streams & High-Performance Aggregations.
 * 
 * Demonstrates:
 * - Primitive Stream types: IntStream, LongStream, DoubleStream.
 * - Range generation: range() vs rangeClosed().
 * - Specialized Summary Statistics: IntSummaryStatistics, DoubleSummaryStatistics.
 * - Primitive Boxing/Unboxing: boxed(), mapToObj(), mapToInt(), mapToDouble().
 * - Senior SRE Insights: Memory layout & GC overhead elimination via primitive streams.
 */
public class PrimitiveStreamsDemo {

    public record MetricSample(String timestamp, int cpuPercent, double memoryMb) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.2.3 PRIMITIVE STREAMS: IntStream, LongStream & DoubleStream");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Creating Primitive Streams & Ranges
        // ==========================================
        System.out.println("\n--- 1. Primitive Stream Ranges (range vs rangeClosed) ---");

        // IntStream.range (exclusive upper bound [1..5))
        int sumRange = IntStream.range(1, 5).sum(); // 1 + 2 + 3 + 4 = 10

        // IntStream.rangeClosed (inclusive upper bound [1..5])
        int sumRangeClosed = IntStream.rangeClosed(1, 5).sum(); // 1 + 2 + 3 + 4 + 5 = 15

        System.out.println("IntStream.range(1, 5).sum()       : " + sumRange);
        System.out.println("IntStream.rangeClosed(1, 5).sum() : " + sumRangeClosed);

        // Generating a series of server hostnames using rangeClosed + mapToObj
        List<String> serverHosts = IntStream.rangeClosed(1, 4)
            .mapToObj(i -> String.format("k8s-node-%02d.internal", i))
            .toList();
        System.out.println("Generated Server Hostnames       : " + serverHosts);

        // ==========================================
        // 2. Object Stream -> Primitive Stream Mapping
        // ==========================================
        System.out.println("\n--- 2. Object Stream to Primitive Stream Mapping ---");

        List<MetricSample> samples = List.of(
            new MetricSample("12:00:00", 45, 1024.5),
            new MetricSample("12:00:05", 88, 2048.0),
            new MetricSample("12:00:10", 92, 4096.2),
            new MetricSample("12:00:15", 30, 1024.0)
        );

        // Map to IntStream for CPU percentage analysis
        IntStream cpuStream = samples.stream().mapToInt(MetricSample::cpuPercent);
        double avgCpu = cpuStream.average().orElse(0.0);

        // Map to DoubleStream for Memory analysis
        DoubleStream memStream = samples.stream().mapToDouble(MetricSample::memoryMb);
        double maxMem = memStream.max().orElse(0.0);

        System.out.printf("Average CPU Usage     : %.2f%%\n", avgCpu);
        System.out.printf("Max Memory Allocated  : %.2f MB\n", maxMem);

        // ==========================================
        // 3. Summary Statistics (Single Pass Aggregation)
        // ==========================================
        System.out.println("\n--- 3. Summary Statistics (IntSummaryStatistics / DoubleSummaryStatistics) ---");

        IntSummaryStatistics cpuStats = samples.stream()
            .mapToInt(MetricSample::cpuPercent)
            .summaryStatistics();

        System.out.println("CPU Stats Count : " + cpuStats.getCount());
        System.out.println("CPU Stats Min   : " + cpuStats.getMin() + "%");
        System.out.println("CPU Stats Max   : " + cpuStats.getMax() + "%");
        System.out.println("CPU Stats Sum   : " + cpuStats.getSum());
        System.out.printf("CPU Stats Avg   : %.2f%%\n", cpuStats.getAverage());

        DoubleSummaryStatistics memStats = samples.stream()
            .mapToDouble(MetricSample::memoryMb)
            .summaryStatistics();

        System.out.printf("Memory Stats    : Min=%.1fMB, Max=%.1fMB, Avg=%.1fMB\n",
            memStats.getMin(), memStats.getMax(), memStats.getAverage());

        // ==========================================
        // 4. Primitive Boxing & Unboxing (boxed())
        // ==========================================
        System.out.println("\n--- 4. Boxing Primitive Streams (.boxed()) ---");

        // Convert IntStream back to Stream<Integer> using boxed()
        List<Integer> boxedList = IntStream.of(10, 20, 30, 40)
            .filter(n -> n > 15)
            .boxed()
            .toList();

        System.out.println("Boxed List<Integer>: " + boxedList);

        // ==========================================
        // 5. LongStream for High-Volume Counter Aggregation
        // ==========================================
        System.out.println("\n--- 5. LongStream High-Volume Counter ---");
        long totalBytes = LongStream.rangeClosed(1, 1_000_000L)
            .filter(b -> b % 2 == 0)
            .sum();

        System.out.println("Sum of 500,000 even Longs: " + totalBytes);

        // ==========================================
        // 6. Senior SRE Performance Insight
        // ==========================================
        System.out.println("\n💡 Senior SRE Memory & Performance Insight:");
        System.out.println("   1. A primitive 'int' occupies 4 bytes of contiguous memory.");
        System.out.println("   2. An 'Integer' object on 64-bit JVM with Compressed OOPs occupies 16-24 bytes (12-byte header + 4-byte payload + padding).");
        System.out.println("   3. Processing 10,000,000 metrics via Stream<Integer> creates ~160MB - 240MB of GC Heap overhead compared to ~40MB with IntStream.");
        System.out.println("   4. Always prefer IntStream/LongStream/DoubleStream for metric processing to maximize L1/L2/L3 CPU cache efficiency!");
    }
}
