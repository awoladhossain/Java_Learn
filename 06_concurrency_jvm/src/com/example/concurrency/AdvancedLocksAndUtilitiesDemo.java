package com.example.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Section 6.1.3: Advanced Locks & Synchronization Utilities.
 * 
 * Demonstrates:
 * - ReentrantLock: Explicit locking, tryLock timeout, and interruptible locks.
 * - ReentrantReadWriteLock: ReadLock (shared) vs WriteLock (exclusive) for read-heavy workloads.
 * - StampedLock: Optimistic reading (validate stamp) for zero-lock overhead read performance.
 * - CountDownLatch: One-shot synchronization countdown barrier.
 * - CyclicBarrier: Reusable multi-thread rendezvous barrier.
 * - Semaphore: Permit-based rate limiter for resource access bounding.
 */
public class AdvancedLocksAndUtilitiesDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.3 ADVANCED LOCKS & SYNCHRONIZATION UTILITIES");
        System.out.println("------------------------------------------------------------------------");

        demoReentrantLock();
        demoReadWriteLock();
        demoStampedLock();
        demoCountDownLatch();
        demoCyclicBarrier();
        demoSemaphore();
    }

    // ==========================================
    // 1. ReentrantLock & tryLock
    // ==========================================
    private static void demoReentrantLock() {
        System.out.println("\n--- 1. ReentrantLock & Timed tryLock ---");
        ReentrantLock lock = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("   [LOCK-1] Acquired lock. Holding for 100ms...");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("   [LOCK-2] Attempting tryLock(30ms)...");
                if (lock.tryLock(30, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("   [LOCK-2] Acquired lock!");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("   [LOCK-2] Could NOT acquire lock within timeout! Fallback triggered cleanly.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==========================================
    // 2. ReentrantReadWriteLock
    // ==========================================
    private static void demoReadWriteLock() {
        System.out.println("\n--- 2. ReentrantReadWriteLock (Shared Readers / Exclusive Writer) ---");
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

        // Multiple threads can acquire ReadLock simultaneously!
        Runnable readerTask = () -> {
            readLock.lock();
            try {
                System.out.println("   [READER] Shared ReadLock acquired by: " + Thread.currentThread().getName());
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                readLock.unlock();
            }
        };

        Runnable writerTask = () -> {
            writeLock.lock();
            try {
                System.out.println("   [WRITER] Exclusive WriteLock acquired by: " + Thread.currentThread().getName());
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
            }
        };

        Thread r1 = new Thread(readerTask, "reader-1");
        Thread r2 = new Thread(readerTask, "reader-2");
        Thread w1 = new Thread(writerTask, "writer-1");

        r1.start();
        r2.start();
        w1.start();

        try {
            r1.join();
            r2.join();
            w1.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==========================================
    // 3. StampedLock (Optimistic Reading)
    // ==========================================
    private static void demoStampedLock() {
        System.out.println("\n--- 3. StampedLock Optimistic Read ---");
        StampedLock stampedLock = new StampedLock();
        
        double x = 10.0, y = 20.0;

        // Try optimistic read (returns stamp, does NOT acquire actual lock!)
        long stamp = stampedLock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;

        // Validate if write lock was acquired by another thread during read
        if (!stampedLock.validate(stamp)) {
            System.out.println("   [STAMPED-LOCK] Optimistic read invalidated! Falling back to read lock...");
            stamp = stampedLock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        } else {
            System.out.println("   [STAMPED-LOCK] Optimistic Read Validated! No lock overhead incurred. Values: (" + currentX + ", " + currentY + ")");
        }
    }

    // ==========================================
    // 4. CountDownLatch
    // ==========================================
    private static void demoCountDownLatch() {
        System.out.println("\n--- 4. CountDownLatch (Subtask Synchronization) ---");
        int totalWorkers = 3;
        CountDownLatch latch = new CountDownLatch(totalWorkers);

        for (int i = 1; i <= totalWorkers; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("   [LATCH WORKER " + id + "] Initializing sub-component...");
                try { Thread.sleep(40); } catch (InterruptedException ignored) {}
                latch.countDown(); // Decrement countdown counter
                System.out.println("   [LATCH WORKER " + id + "] Completed! Latch count: " + latch.getCount());
            }).start();
        }

        try {
            System.out.println("   [MAIN] Awaiting all 3 sub-components to complete...");
            latch.await(); // Main thread blocks until countdown hits 0
            System.out.println("   [MAIN] All sub-components initialized successfully!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==========================================
    // 5. CyclicBarrier
    // ==========================================
    private static void demoCyclicBarrier() {
        System.out.println("\n--- 5. CyclicBarrier (Multi-Thread Rendezvous) ---");
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> 
            System.out.println("   🎉 [BARRIER ACTION] All " + parties + " threads reached barrier! Proceeding to next phase..."));

        for (int i = 1; i <= parties; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("   [BARRIER WORKER " + id + "] Completed Phase 1. Waiting at barrier...");
                    barrier.await(); // Wait at rendezvous point
                    System.out.println("   [BARRIER WORKER " + id + "] Resumed Phase 2!");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    }

    // ==========================================
    // 6. Semaphore
    // ==========================================
    private static void demoSemaphore() {
        System.out.println("\n--- 6. Semaphore (Resource Rate Limiting) ---");
        int maxPermits = 2; // Only 2 concurrent connections allowed!
        Semaphore dbPoolSemaphore = new Semaphore(maxPermits);

        Runnable queryTask = () -> {
            try {
                dbPoolSemaphore.acquire(); // Acquire 1 permit
                System.out.println("   [SEMAPHORE] Acquired DB Connection permit. Thread: " + Thread.currentThread().getName());
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("   [SEMAPHORE] Releasing DB Connection permit. Thread: " + Thread.currentThread().getName());
                dbPoolSemaphore.release(); // Release permit back to pool
            }
        };

        for (int i = 1; i <= 4; i++) {
            new Thread(queryTask, "db-worker-" + i).start();
        }

        try { Thread.sleep(150); } catch (InterruptedException ignored) {}
    }
}
