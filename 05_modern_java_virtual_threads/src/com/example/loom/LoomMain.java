package com.example.loom;

/**
 * Main Runner Class for Phase 5.4: Project Loom & Concurrency Evolution (Java 21+).
 * 
 * Executes comprehensive demonstrations covering:
 * - 5.4.1 Virtual Threads (Platform vs Virtual Threads, ExecutorService, 100k Concurrency, Pinning hazards).
 * - 5.4.2 Structured Concurrency (ShutdownOnFailure, ShutdownOnSuccess, orphan task prevention).
 * - 5.4.3 Scoped Values (ThreadLocal replacement, immutable context binding, zero memory leaks).
 */
public class LoomMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("⚡ PHASE 5.4: PROJECT LOOM & VIRTUAL THREADS CONCURRENCY DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Virtual Threads Foundations & High Concurrency Benchmark
        VirtualThreadsDemo.runDemo();

        // 2. Structured Concurrency (ShutdownOnFailure & ShutdownOnSuccess)
        StructuredConcurrencyDemo.runDemo();

        // 3. Scoped Values (ThreadLocal Replacement)
        ScopedValuesDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 5.4 PROJECT LOOM CONCURRENCY EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
