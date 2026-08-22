package com.example.streams;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Section 5.2.4: Parallel Streams & Concurrency Hazards.
 * 
 * Demonstrates:
 * - Parallel Stream Mechanics (.parallelStream() vs .sequential()).
 * - ForkJoinPool.commonPool() utilization & parallelism factor.
 * - SRE Thread Starvation Hazard: Blocking I/O inside commonPool.
 * - Resource Isolation: Executing parallel streams in custom ForkJoinPool.
 * - Thread Safety Pitfalls: Shared mutable state vs thread-safe Collectors.
 * - Benchmark insights: When parallel streams help vs hurt performance (N * Q rule).
 */
public class ParallelStreamsDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.2.4 PARALLEL STREAMS: ForkJoinPool, Isolation & Starvation Hazards");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Parallel Stream Mechanics & Thread Pool
        // ==========================================
        System.out.println("\n--- 1. ForkJoinPool.commonPool() Mechanics ---");

        int commonPoolParallelism = ForkJoinPool.commonPool().getParallelism();
        System.out.println("Available CPU Cores          : " + Runtime.getRuntime().availableProcessors());
        System.out.println("ForkJoinPool.commonPool() Size: " + commonPoolParallelism);

        System.out.println("Executing parallelStream thread inspection:");
        IntStream.rangeClosed(1, 5)
            .parallel()
            .forEach(i -> {
                System.out.printf("   Task %d processed by thread: %s\n", i, Thread.currentThread().getName());
            });

        // ==========================================
        // 2. Thread Safety & Shared Mutable State Pitfall
        // ==========================================
        System.out.println("\n--- 2. Thread Safety Pitfall (Side-Effects vs Pure Collectors) ---");

        // Correct Functional Way: Use Collectors.toList() which handles thread safety internally
        List<Integer> safeCollectedList = IntStream.rangeClosed(1, 1000)
            .parallel()
            .filter(n -> n % 2 == 0)
            .boxed()
            .collect(Collectors.toList());

        System.out.println("Safe Collector result size   : " + safeCollectedList.size() + " (Expected: 500)");

        // ConcurrentHashMap accumulation
        ConcurrentHashMap<String, Integer> threadMap = new ConcurrentHashMap<>();
        IntStream.rangeClosed(1, 100)
            .parallel()
            .forEach(i -> threadMap.merge(Thread.currentThread().getName(), 1, Integer::sum));

        System.out.println("Thread Task Distribution Map : ");
        threadMap.forEach((threadName, count) -> 
            System.out.println("   Thread [" + threadName + "] processed " + count + " items"));

        // ==========================================
        // 3. Thread Starvation Hazard & Custom ForkJoinPool Isolation
        // ==========================================
        System.out.println("\n--- 3. Custom ForkJoinPool Resource Isolation ---");
        System.out.println("Demonstrating custom pool isolation to prevent shared commonPool starvation...");

        // Dedicated custom thread pool for isolated background stream tasks
        try (ForkJoinPool customPool = new ForkJoinPool(4)) {
            List<String> results = customPool.submit(() -> 
                IntStream.rangeClosed(1, 8)
                    .parallel()
                    .mapToObj(taskId -> {
                        String threadName = Thread.currentThread().getName();
                        // Simulating light task execution
                        return String.format("Task-%d processed on [%s]", taskId, threadName);
                    })
                    .collect(Collectors.toList())
            ).get(5, TimeUnit.SECONDS);

            System.out.println("Custom Pool Execution Successful. Processed " + results.size() + " tasks.");
            results.forEach(res -> System.out.println("   " + res));

        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }

        // ==========================================
        // 4. Benchmarking: N * Q Performance Rule
        // ==========================================
        System.out.println("\n--- 4. N * Q Rule: Sequential vs Parallel Comparison ---");
        int elementCount = 5_000_000;

        // Sequential Computation
        long startSeq = System.nanoTime();
        double seqResult = IntStream.rangeClosed(1, elementCount)
            .mapToDouble(i -> Math.sin(i) * Math.cos(i))
            .sum();
        long seqDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSeq);

        // Parallel Computation
        long startPar = System.nanoTime();
        double parResult = IntStream.rangeClosed(1, elementCount)
            .parallel()
            .mapToDouble(i -> Math.sin(i) * Math.cos(i))
            .sum();
        long parDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startPar);

        System.out.printf("Sequential Execution Time : %d ms (Result: %.2f)\n", seqDurationMs, seqResult);
        System.out.printf("Parallel Execution Time   : %d ms (Result: %.2f)\n", parDurationMs, parResult);
        if (seqDurationMs > 0) {
            System.out.printf("Parallel Speedup Ratio    : %.2fx faster\n", (double) seqDurationMs / parDurationMs);
        }

        // ==========================================
        // 5. Senior SRE Critical Golden Rules
        // ==========================================
        System.out.println("\n💡 Senior SRE Parallel Stream Golden Rules:");
        System.out.println("   1. NEVER execute blocking I/O (Database calls, HTTP requests, File disk I/O) in parallel streams!");
        System.out.println("      Doing so blocks threads in ForkJoinPool.commonPool(), stalling all other parallel streams JVM-wide.");
        System.out.println("   2. Never mutate shared non-thread-safe collections (e.g. ArrayList, HashMap) inside .forEach() of a parallel stream.");
        System.out.println("   3. Apply Parallel Streams ONLY when N * Q is large (N = number of elements > 10,000, Q = CPU computation cost per element).");
        System.out.println("   4. If isolated concurrency is required for heavy computations, isolate the stream inside a dedicated custom ForkJoinPool.");
    }
}
