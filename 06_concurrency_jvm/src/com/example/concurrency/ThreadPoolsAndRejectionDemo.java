package com.example.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Section 6.1.4: Thread Pools, ThreadPoolExecutor & Task Rejection Policies.
 * 
 * Demonstrates:
 * - ThreadPoolExecutor core parameters: corePoolSize, maxPoolSize, keepAliveTime, workQueue.
 * - Standard Thread Pool types: FixedThreadPool, CachedThreadPool, ScheduledExecutorService.
 * - All 4 Rejection Policies: AbortPolicy, CallerRunsPolicy, DiscardPolicy, DiscardOldestPolicy.
 * - Senior SRE Thread Pool Tuning: Preventing unbounded queue OOMs & backpressure control.
 */
public class ThreadPoolsAndRejectionDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.4 THREAD POOLS & TASK REJECTION POLICIES");
        System.out.println("------------------------------------------------------------------------");

        demoScheduledThreadPool();
        demoRejectionPolicies();
    }

    // ==========================================
    // 1. ScheduledExecutorService (Periodic Tasks)
    // ==========================================
    private static void demoScheduledThreadPool() {
        System.out.println("\n--- 1. ScheduledExecutorService (Cron / Periodic Heartbeat) ---");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Schedule periodic heartbeat every 50ms
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("   [HEARTBEAT] Metric collector pinging telemetry backend at " + System.currentTimeMillis());
        }, 10, 50, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(160); // Allow 3 heartbeats to fire
        } catch (InterruptedException ignored) {
        } finally {
            scheduler.shutdown();
        }
    }

    // ==========================================
    // 2. ThreadPoolExecutor Rejection Policies
    // ==========================================
    private static void demoRejectionPolicies() {
        System.out.println("\n--- 2. ThreadPoolExecutor Rejection Policies ---");

        // Small Bounded Pool Configuration:
        // corePoolSize = 1, maxPoolSize = 1, queueCapacity = 2
        // Total capacity = 1 running task + 2 queued tasks = 3 tasks max.
        // Task #4 will trigger rejection!

        // A. AbortPolicy (Throws RejectedExecutionException)
        System.out.println("\n--- A. AbortPolicy (Default Fail-Fast) ---");
        try (ThreadPoolExecutor abortPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2),
            new ThreadPoolExecutor.AbortPolicy()
        )) {
            try {
                for (int i = 1; i <= 4; i++) {
                    final int taskId = i;
                    abortPool.submit(() -> blockTask(taskId));
                }
            } catch (RejectedExecutionException e) {
                System.out.println("   [REJECTION-ABORT] Task #4 was rejected with exception: " + e.getClass().getSimpleName());
            } finally {
                abortPool.shutdownNow();
            }
        }

        // B. CallerRunsPolicy (Caller Thread Executes Task -> Natural Backpressure!)
        System.out.println("\n--- B. CallerRunsPolicy (Backpressure Control) ---");
        try (ThreadPoolExecutor callerRunsPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2),
            new ThreadPoolExecutor.CallerRunsPolicy()
        )) {
            for (int i = 1; i <= 4; i++) {
                final int taskId = i;
                System.out.println("   Submitting Task #" + taskId + " from thread: " + Thread.currentThread().getName());
                callerRunsPool.submit(() -> blockTask(taskId));
            }

            callerRunsPool.shutdown();
            try { callerRunsPool.awaitTermination(1, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        }

        // C. DiscardPolicy (Silently Drops Overflow Tasks)
        System.out.println("\n--- C. DiscardPolicy (Silent Drop) ---");
        try (ThreadPoolExecutor discardPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2),
            new ThreadPoolExecutor.DiscardPolicy()
        )) {
            for (int i = 1; i <= 4; i++) {
                final int taskId = i;
                discardPool.submit(() -> blockTask(taskId));
            }
            System.out.println("   [REJECTION-DISCARD] Submitted 4 tasks to pool with capacity 3. Task #4 was silently dropped.");
            discardPool.shutdownNow();
        }

        // ==========================================
        // 3. Senior SRE Thread Pool Tuning Golden Rules
        // ==========================================
        System.out.println("\n💡 Senior SRE Production Thread Pool Rules:");
        System.out.println("   1. NEVER use Executors.newFixedThreadPool() or newSingleThreadExecutor() with default LinkedBlockingQueue in production!");
        System.out.println("      Default LinkedBlockingQueue has Integer.MAX_VALUE capacity, causing OutOfMemoryError during traffic spikes.");
        System.out.println("   2. ALWAYS use explicit ThreadPoolExecutor with a BOUNDED queue (e.g. ArrayBlockingQueue(1000)).");
        System.out.println("   3. Use CallerRunsPolicy when producer threads need backpressure to slow down request ingestion.");
        System.out.println("   4. Name custom thread pool threads (via ThreadFactory) for easy identification in jstack thread dumps.");
    }

    private static void blockTask(int taskId) {
        System.out.printf("   Task #%d executing on thread: %s\n", taskId, Thread.currentThread().getName());
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
    }
}
