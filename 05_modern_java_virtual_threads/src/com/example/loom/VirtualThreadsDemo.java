package com.example.loom;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Section 5.4.1: Virtual Threads & Project Loom Foundations.
 * 
 * Demonstrates:
 * - Platform Threads (OS-bound 1:1 mapping) vs Virtual Threads (Carrier-bound M:N mapping).
 * - Creation APIs: Thread.ofVirtual().start(), Thread.ofVirtual().factory(), Executors.newVirtualThreadPerTaskExecutor().
 * - SRE Pinning Hazard: Synchronized blocks pinning Virtual Threads to Carrier OS threads vs ReentrantLock.
 * - Massive Concurrency Benchmark: Spawning 100,000 concurrent Virtual Threads without OOM.
 */
public class VirtualThreadsDemo {

    private static final ReentrantLock safeLock = new ReentrantLock();

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.4.1 VIRTUAL THREADS: Platform Threads vs Virtual Threads (Project Loom)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Thread Mechanics & Property Inspection
        // ==========================================
        System.out.println("\n--- 1. Thread Property & Mechanics Inspection ---");

        Thread platformThread = Thread.ofPlatform().name("platform-worker-1").unstarted(() -> {
            System.out.println("   [PLATFORM] Thread Name: " + Thread.currentThread().getName() + 
                               " | isVirtual: " + Thread.currentThread().isVirtual());
        });
        platformThread.start();

        Thread virtualThread = Thread.ofVirtual().name("virtual-worker-1").start(() -> {
            System.out.println("   [VIRTUAL] Thread Name: " + Thread.currentThread().getName() + 
                               " | isVirtual: " + Thread.currentThread().isVirtual());
        });

        try {
            platformThread.join();
            virtualThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ==========================================
        // 2. Creating Virtual Threads via Executors.newVirtualThreadPerTaskExecutor()
        // ==========================================
        System.out.println("\n--- 2. Executors.newVirtualThreadPerTaskExecutor() ---");

        // VirtualThreadPerTaskExecutor creates a fresh Virtual Thread for EVERY submitted task!
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.printf("   Task %d executing on Virtual Thread: %s (Carrier: %s)\n",
                            taskId, Thread.currentThread(), Thread.currentThread().toString());
                    try {
                        Thread.sleep(Duration.ofMillis(50)); // Unmounts Virtual Thread from Carrier OS Thread during sleep!
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // Executor auto-closes at end of block, waiting for all virtual threads to complete

        // ==========================================
        // 3. Thread Pinning Hazard & ReentrantLock Fix
        // ==========================================
        System.out.println("\n--- 3. SRE Thread Pinning Hazard vs ReentrantLock ---");

        System.out.println("   [PINNING HAZARD] When a Virtual Thread enters a 'synchronized' block/method,");
        System.out.println("   it PINNS to its underlying Carrier OS Thread. If it blocks during pinning, the OS thread cannot execute other Virtual Threads!");
        System.out.println("   [SOLUTION] Replace 'synchronized' blocks with 'java.util.concurrent.locks.ReentrantLock' inside virtual thread code paths.");

        // Non-pinning locking using ReentrantLock
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                safeLock.lock();
                try {
                    System.out.println("   [SAFE LOCK] Virtual Thread safely acquired ReentrantLock without pinning carrier thread!");
                } finally {
                    safeLock.unlock();
                }
            });
        }

        // ==========================================
        // 4. Massive Concurrency Benchmark (100,000 Virtual Threads)
        // ==========================================
        System.out.println("\n--- 4. Massive Concurrency Benchmark (100,000 Virtual Threads) ---");

        int taskCount = 100_000;
        AtomicInteger completedTasks = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100)); // Simulating 100ms blocking I/O latency (Database query / REST API call)
                        completedTasks.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // Block awaits all 100,000 virtual threads

        long durationMs = System.currentTimeMillis() - startTime;

        System.out.printf("Successfully executed %d concurrent Virtual Threads in %d ms!\n", completedTasks.get(), durationMs);

        // ==========================================
        // 5. Senior SRE Architectural Rules for Virtual Threads
        // ==========================================
        System.out.println("\n💡 Senior SRE Golden Rules for Virtual Threads:");
        System.out.println("   1. NEVER POOL Virtual Threads! Virtual Threads are cheap and short-lived. Create a new Virtual Thread per task.");
        System.out.println("   2. Virtual Threads are designed for I/O-bound tasks (Database queries, HTTP calls, File I/O), NOT CPU-bound computations.");
        System.out.println("   3. Replace 'synchronized' with 'ReentrantLock' on critical I/O paths to avoid pinning carrier OS threads.");
        System.out.println("   4. Monitor pinned threads using the JVM flag: -Djdk.tracePinnedThreads=full");
    }
}
