package com.example.concurrency;

/**
 * Main Runner Class for Phase 6.1: Java Memory Model (JMM) & Multithreading.
 * 
 * Executes comprehensive demonstrations covering:
 * - 6.1.1 Thread Lifecycle & Creation (States, Runnable, Callable, Future, CompletableFuture).
 * - 6.1.2 JMM & Synchronization (Happens-Before, volatile visibility vs atomicity, synchronized reentrancy).
 * - 6.1.3 Advanced Locks & Utilities (ReentrantLock, ReadWriteLock, StampedLock, CountDownLatch, CyclicBarrier, Semaphore).
 * - 6.1.4 Thread Pools & Rejection (ThreadPoolExecutor parameters, ScheduledExecutorService, 4 Rejection Policies).
 * - 6.1.5 Atomic Variables & Lock-Free (CAS primitives, AtomicReference Stack, LongAdder vs AtomicLong benchmark).
 * - 6.1.6 Concurrency Diagnostics & Deadlocks (Controlled Deadlock, ThreadMXBean, jstack / jcmd thread dump analysis).
 */
public class ConcurrencyMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("🧠 PHASE 6.1: JAVA MEMORY MODEL (JMM) & MULTITHREADING DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Thread Lifecycle & Creation Mechanics
        ThreadLifecycleAndCreationDemo.runDemo();

        // 2. JMM, Volatile & Intrinsic Synchronization
        JmmAndSynchronizationDemo.runDemo();

        // 3. Advanced Locks & Synchronization Utilities
        AdvancedLocksAndUtilitiesDemo.runDemo();

        // 4. Thread Pools & Task Rejection Policies
        ThreadPoolsAndRejectionDemo.runDemo();

        // 5. Atomic Variables & Lock-Free Concurrency
        AtomicAndLockFreeDemo.runDemo();

        // 6. Concurrency Diagnostics & Deadlock Analysis
        ConcurrencyDiagnosticsDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 6.1 JMM & MULTITHREADING EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
