package com.example.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Section 6.1.5: Atomic Variables & Lock-Free Concurrency (CAS).
 * 
 * Demonstrates:
 * - Compare-And-Swap (CAS) Hardware Primitives.
 * - AtomicInteger & AtomicReference lock-free operations.
 * - LongAdder vs AtomicLong under High Thread Contention (Striped Cell Counters).
 * - SRE Performance Benchmarks: Lock-Free vs Synchronized vs Striped Adders.
 */
public class AtomicAndLockFreeDemo {

    // Lock-Free Stack Node for AtomicReference demonstration
    public record StackNode<T>(T value, StackNode<T> next) {}

    public static class LockFreeStack<T> {
        private final AtomicReference<StackNode<T>> top = new AtomicReference<>(null);

        public void push(T value) {
            StackNode<T> newHead = new StackNode<>(value, null);
            StackNode<T> oldHead;
            do {
                oldHead = top.get();
                newHead = new StackNode<>(value, oldHead);
            } while (!top.compareAndSet(oldHead, newHead)); // CAS loop!
        }

        public T pop() {
            StackNode<T> oldHead;
            StackNode<T> newHead;
            do {
                oldHead = top.get();
                if (oldHead == null) return null;
                newHead = oldHead.next();
            } while (!top.compareAndSet(oldHead, newHead)); // CAS loop!
            return oldHead.value();
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.5 ATOMIC VARIABLES & LOCK-FREE CONCURRENCY (CAS)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. AtomicInteger & CAS Operations
        // ==========================================
        System.out.println("\n--- 1. AtomicInteger & Compare-And-Swap (CAS) ---");
        AtomicInteger counter = new AtomicInteger(100);

        // compareAndSet(expectedValue, newValue)
        boolean success1 = counter.compareAndSet(100, 200); // Succeeds
        boolean success2 = counter.compareAndSet(100, 300); // Fails (current value is 200)

        System.out.println("CAS (100 -> 200) Success? " + success1 + " | Current Value: " + counter.get());
        System.out.println("CAS (100 -> 300) Success? " + success2 + " | Current Value: " + counter.get());

        // Functional atomic update
        int updatedVal = counter.updateAndGet(val -> val * 2 + 50); // 200 * 2 + 50 = 450
        System.out.println("Atomic updateAndGet Result: " + updatedVal);

        // ==========================================
        // 2. Lock-Free Stack using AtomicReference
        // ==========================================
        System.out.println("\n--- 2. Lock-Free Data Structure (AtomicReference Stack) ---");
        LockFreeStack<String> stack = new LockFreeStack<>();

        stack.push("req-001");
        stack.push("req-002");
        stack.push("req-003");

        System.out.println("Popped from Lock-Free Stack: " + stack.pop());
        System.out.println("Popped from Lock-Free Stack: " + stack.pop());

        // ==========================================
        // 3. High-Contention Benchmark: AtomicLong vs LongAdder
        // ==========================================
        System.out.println("\n--- 3. High-Contention Benchmark: AtomicLong vs LongAdder ---");

        int threadCount = 16;
        int operationsPerThread = 1_000_000;

        // A. AtomicLong Contention Benchmark
        AtomicInteger atomicCounter = new AtomicInteger(0);
        long startAtomic = System.nanoTime();
        runBenchmark(threadCount, operationsPerThread, atomicCounter::incrementAndGet);
        long durationAtomicMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startAtomic);

        // B. LongAdder Contention Benchmark (Striped Cell Counter)
        LongAdder longAdder = new LongAdder();
        long startAdder = System.nanoTime();
        runBenchmark(threadCount, operationsPerThread, longAdder::increment);
        long durationAdderMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startAdder);

        System.out.printf("AtomicInteger Execution Time : %d ms (Final Value: %d)\n", durationAtomicMs, atomicCounter.get());
        System.out.printf("LongAdder Execution Time     : %d ms (Final Value: %d)\n", durationAdderMs, longAdder.sum());
        if (durationAdderMs > 0) {
            System.out.printf("LongAdder Performance Gain   : %.2fx faster under high contention!\n", (double) durationAtomicMs / durationAdderMs);
        }

        // ==========================================
        // 4. Senior SRE Lock-Free Principles
        // ==========================================
        System.out.println("\n💡 Senior SRE Lock-Free Concurrency Rules:");
        System.out.println("   1. CAS instructions operate at hardware CPU level without OS thread blocking or context switches.");
        System.out.println("   2. Under HIGH thread write contention, AtomicLong suffers from CPU cache line bounce due to CAS retry loops.");
        System.out.println("   3. Prefer 'LongAdder' or 'DoubleAdder' for high-throughput metrics/counters (e.g. HTTP request counters).");
    }

    private static void runBenchmark(int threads, int ops, Runnable action) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < ops; j++) {
                        action.run();
                    }
                });
            }
        }
    }
}
