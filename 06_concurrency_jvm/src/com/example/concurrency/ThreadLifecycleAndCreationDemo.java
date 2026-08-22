package com.example.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Section 6.1.1: Thread Lifecycle & Thread Creation Mechanics.
 * 
 * Demonstrates:
 * - Thread States: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
 * - Thread Creation Methods: Thread class, Runnable, Callable<V>, Future<V>.
 * - Asynchronous Pipelines: CompletableFuture<V> composition (thenApply, thenCombine, exceptionally).
 */
public class ThreadLifecycleAndCreationDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.1.1 THREAD LIFECYCLE & CREATION MECHANICS");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Thread States & Lifecycle Transitions
        // ==========================================
        System.out.println("\n--- 1. Thread Lifecycle States ---");

        Object lock = new Object();
        Thread workerThread = new Thread(() -> {
            try {
                // State: RUNNABLE -> TIMED_WAITING
                Thread.sleep(100);

                // State: WAITING on lock object
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "lifecycle-worker");

        // State: NEW
        System.out.println("State after creation: " + workerThread.getState());

        workerThread.start();
        // State: RUNNABLE
        System.out.println("State after start() : " + workerThread.getState());

        try {
            Thread.sleep(30);
            // State: TIMED_WAITING (during sleep)
            System.out.println("State during sleep(): " + workerThread.getState());

            Thread.sleep(120);
            // State: WAITING (during wait())
            System.out.println("State during wait() : " + workerThread.getState());

            // Notify worker thread to unblock
            synchronized (lock) {
                lock.notify();
            }

            workerThread.join();
            // State: TERMINATED
            System.out.println("State after join()  : " + workerThread.getState());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ==========================================
        // 2. Creating Threads: Runnable vs Callable & Future
        // ==========================================
        System.out.println("\n--- 2. Callable<V> & Future<V> ---");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // A. Runnable (No return value, cannot throw checked exceptions)
        Runnable runnableTask = () -> System.out.println("   [RUNNABLE] Executing asynchronous side-effect task...");
        executor.execute(runnableTask);

        // B. Callable (Returns value, can throw checked exceptions)
        Callable<String> callableTask = () -> {
            System.out.println("   [CALLABLE] Executing database query in background...");
            Thread.sleep(50);
            return "QUERY_RESULT_OK [ID: 99482]";
        };

        Future<String> future = executor.submit(callableTask);

        try {
            // Future.get() blocks until result is ready
            String result = future.get(2, TimeUnit.SECONDS);
            System.out.println("   [FUTURE] Received Result: " + result);
        } catch (Exception e) {
            System.err.println("Future execution failed: " + e.getMessage());
        }

        // ==========================================
        // 3. Modern Asynchronous Pipelines (CompletableFuture)
        // ==========================================
        System.out.println("\n--- 3. CompletableFuture Async Pipelines ---");

        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("   [CF-1] Fetching User profile from Auth Service...");
            return "User: Alex";
        }, executor);

        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("   [CF-2] Fetching Orders from Order Service...");
            return "Orders: [Apples, Books]";
        }, executor);

        // Combining two independent futures asynchronously
        CompletableFuture<String> combinedFuture = userFuture.thenCombineAsync(orderFuture, 
            (user, orders) -> user + " | " + orders, executor);

        try {
            System.out.println("   [CF-COMBINED] Result: " + combinedFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("CompletableFuture error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
