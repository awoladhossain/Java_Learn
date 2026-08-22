package com.example.concurrency;

/**
 * Section 6.1.2: Java Memory Model (JMM), Volatile & Intrinsic Synchronization.
 * 
 * Demonstrates:
 * - JMM Architecture: Main Memory vs Thread Working Memory (CPU L1/L2/L3 Caches).
 * - Happens-Before Relationship Rules & Instruction Reordering.
 * - volatile Keyword: Memory Visibility & Memory Barriers vs Lack of Compound Atomicity.
 * - Intrinsic Locks (synchronized): Object Monitor vs Class Monitor Locks & Lock Reentrancy.
 */
public class JmmAndSynchronizationDemo {

    // Volatile flag guaranteeing visibility across threads
    private static volatile boolean running = true;
    private static volatile int volatileCounter = 0;
    private static int nonVolatileCounter = 0;

    // Monitor lock target object
    private final Object lock = new Object();
    private int synchronizedCounter = 0;

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.2 JAVA MEMORY MODEL (JMM) & SYNCHRONIZATION");
        System.out.println("------------------------------------------------------------------------");

        JmmAndSynchronizationDemo demo = new JmmAndSynchronizationDemo();

        // ==========================================
        // 1. Memory Visibility & volatile Keyword
        // ==========================================
        System.out.println("\n--- 1. Volatile Memory Visibility ---");

        Thread worker = new Thread(() -> {
            int count = 0;
            // Without 'volatile', CPU cache reordering could cause infinite loop because thread never sees running = false!
            while (running) {
                count++;
            }
            System.out.println("   [VOLATILE WORKER] Stopped gracefully after " + count + " iterations.");
        });

        worker.start();
        try {
            Thread.sleep(50);
            running = false; // Main thread writes to volatile field (Flushed to Main Memory immediately!)
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ==========================================
        // 2. Volatile vs Atomicity (The compound count++ Trap)
        // ==========================================
        System.out.println("\n--- 2. Volatile vs Atomicity Trap ---");

        volatileCounter = 0;
        nonVolatileCounter = 0;

        Thread t1 = new Thread(demo::incrementCounters);
        Thread t2 = new Thread(demo::incrementCounters);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Expected Counter Value (2 x 10,000) : 20000");
        System.out.println("Volatile Counter Actual Value      : " + volatileCounter + " (Lost updates due to non-atomic read-modify-write!)");
        System.out.println("Non-Volatile Counter Actual Value  : " + nonVolatileCounter);

        // ==========================================
        // 3. Intrinsic Locks (synchronized) & Reentrancy
        // ==========================================
        System.out.println("\n--- 3. Intrinsic Locks & Lock Reentrancy ---");

        Thread syncThread1 = new Thread(() -> demo.performSynchronizedWork("Thread-A"));
        Thread syncThread2 = new Thread(() -> demo.performSynchronizedWork("Thread-B"));

        syncThread1.start();
        syncThread2.start();

        try {
            syncThread1.join();
            syncThread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Synchronized Counter Final Value   : " + demo.synchronizedCounter + " (100% Thread-Safe!)");

        // ==========================================
        // 4. Senior SRE JMM Happens-Before Rules
        // ==========================================
        System.out.println("\n💡 Senior SRE Happens-Before Principles:");
        System.out.println("   1. Volatile Variable Rule: A write to a volatile field happens-before every subsequent read of that volatile field.");
        System.out.println("   2. Monitor Lock Rule: An unlock on a monitor lock happens-before every subsequent lock on that same monitor.");
        System.out.println("   3. Thread Start/Join Rule: A call to Thread.start() happens-before any action in the started thread.");
        System.out.println("   4. Memory Barriers: Volatile inserts LoadLoad, LoadStore, StoreStore, and StoreLoad barriers to prevent CPU instruction reordering.");
    }

    private void incrementCounters() {
        for (int i = 0; i < 10_000; i++) {
            volatileCounter++;    // Read-Modify-Write (NOT ATOMIC!)
            nonVolatileCounter++; // Read-Modify-Write (NOT ATOMIC!)
        }
    }

    private void performSynchronizedWork(String threadName) {
        synchronized (lock) { // Object Monitor Lock Acquisition
            System.out.println("   [" + threadName + "] Acquired Intrinsic Monitor Lock.");
            reentrantMethod(threadName); // Demonstrating Lock Reentrancy
        } // Monitor Lock Release (Happens-Before relationship established!)
    }

    private void reentrantMethod(String threadName) {
        synchronized (lock) { // Re-entering lock already held by current thread (Lock count = 2)
            for (int i = 0; i < 5_000; i++) {
                synchronizedCounter++;
            }
            System.out.println("   [" + threadName + "] Re-entered lock safely. Incremented synchronizedCounter.");
        } // Lock count = 1
    }
}
