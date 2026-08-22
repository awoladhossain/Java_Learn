package com.example.concurrency;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Section 6.1.6: Concurrency Diagnostics, Deadlock Detection & Thread Dumps.
 * 
 * Demonstrates:
 * - Deadlock Creation: Simulated classic circular wait condition between 2 threads.
 * - Programmatic Deadlock Detection: Using JVM ThreadMXBean.findDeadlockedThreads().
 * - Thread Dump Generation: Extracting jstack / jcmd diagnostic thread state traces.
 * - SRE Troubleshooting: Identifying BLOCKED thread stacks & monitor lock ownership.
 */
public class ConcurrencyDiagnosticsDemo {

    private static final Object resourceA = new Object();
    private static final Object resourceB = new Object();

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.6 CONCURRENCY DIAGNOSTICS & DEADLOCK ANALYSIS");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("\n--- 1. Simulating Controlled Deadlock Condition ---");

        // Thread 1: Acquires Resource A -> Tries to acquire Resource B
        Thread thread1 = new Thread(() -> {
            synchronized (resourceA) {
                System.out.println("   [THREAD-1] Acquired Resource A. Waiting for Resource B...");
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}

                synchronized (resourceB) {
                    System.out.println("   [THREAD-1] Acquired Resource B!");
                }
            }
        }, "deadlock-thread-1");

        // Thread 2: Acquires Resource B -> Tries to acquire Resource A (Circular Wait!)
        Thread thread2 = new Thread(() -> {
            synchronized (resourceB) {
                System.out.println("   [THREAD-2] Acquired Resource B. Waiting for Resource A...");
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}

                synchronized (resourceA) {
                    System.out.println("   [THREAD-2] Acquired Resource A!");
                }
            }
        }, "deadlock-thread-2");

        thread1.start();
        thread2.start();

        // Allow threads to enter deadlocked state
        try { Thread.sleep(150); } catch (InterruptedException ignored) {}

        // ==========================================
        // 2. Programmatic Deadlock Detection via ThreadMXBean
        // ==========================================
        System.out.println("\n--- 2. JVM ThreadMXBean Programmatic Deadlock Detection ---");
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();

        if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
            System.out.println("⚠️  [DEADLOCK DETECTED] Found " + deadlockedThreadIds.length + " deadlocked threads!");

            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds, true, true);
            for (ThreadInfo info : threadInfos) {
                System.out.println("\n--------------------------------------------------");
                System.out.println("Thread Name     : " + info.getThreadName() + " (ID: " + info.getThreadId() + ")");
                System.out.println("Thread State    : " + info.getThreadState());
                System.out.println("Waiting on Lock : " + info.getLockName());
                System.out.println("Lock Owner      : " + info.getLockOwnerName() + " (ID: " + info.getLockOwnerId() + ")");
                System.out.println("Stack Trace Snippet:");
                for (StackTraceElement ste : info.getStackTrace()) {
                    if (ste.getClassName().startsWith("com.example")) {
                        System.out.println("   at " + ste);
                    }
                }
            }
        } else {
            System.out.println("No deadlocked threads found.");
        }

        // ==========================================
        // 3. Senior SRE Thread Dump CLI Diagnostic Commands
        // ==========================================
        System.out.println("\n💡 Senior SRE Production Thread Dump Diagnostic Commands:");
        System.out.println("   1. Generate Thread Dump via jcmd:");
        System.out.println("      $ jcmd <PID> Thread.print > thread_dump.txt");
        System.out.println("   2. Generate Thread Dump via jstack:");
        System.out.println("      $ jstack -l <PID> > thread_dump.txt");
        System.out.println("   3. Key states to look for during incident response:");
        System.out.println("      - BLOCKED on <0x...>: Threads waiting to acquire an intrinsic monitor lock.");
        System.out.println("      - WAITING / TIMED_WAITING (parking): Threads waiting in thread pools or locks.");
        System.out.println("      - High RUNNABLE thread count: Indicates CPU saturation / infinite loops.");

        // Interrupt threads to clean up background execution
        thread1.interrupt();
        thread2.interrupt();
    }
}
