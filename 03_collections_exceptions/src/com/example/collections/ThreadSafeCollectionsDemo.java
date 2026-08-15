package com.example.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3.2.5 Thread-Safe Collections Deep-Dive
 * 
 * SRE & Internals Breakdown:
 * 1. Collections.synchronizedMap():
 *    - Wrapper around a standard Map. Synchronizes ALL operations on a single global mutex lock (this / mutex).
 *    - Iteration requires explicit manual synchronization block on the map object; failure causes ConcurrentModificationException!
 *    - Creates severe lock contention bottlenecks in multi-core high-throughput services.
 * 
 * 2. ConcurrentHashMap (Java 8+):
 *    - Uses Lock-Free CAS (Compare-And-Swap) for empty bin initialization (casTabAt).
 *    - Synchronizes ONLY the first Node of a bucket during insertion/collision (fine-grained bucket-level lock).
 *    - Volatile node values (val) and next pointers allow lock-free concurrent reads!
 *    - Offers atomic composite operations: putIfAbsent(), computeIfAbsent().
 * 
 * 3. CopyOnWriteArrayList:
 *    - Mutating operations (add, set, remove) create a brand-new copy of the underlying array (volatile Object[] array).
 *    - Iterators operate on an immutable array snapshot created at the moment the iterator was constructed.
 *    - Lock-free reads; zero ConcurrentModificationException risk during iteration.
 *    - Ideal for read-heavy, low-write workloads (e.g., system event listeners, microservice feature flags).
 */
public class ThreadSafeCollectionsDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.2.5 THREAD-SAFE COLLECTIONS: synchronizedMap vs ConcurrentHashMap vs CopyOnWriteArrayList");
        System.out.println("------------------------------------------------------------------------");

        demonstrateSynchronizedMapContentionAndIteration();
        demonstrateConcurrentHashMapThroughputAndAtomicOps();
        demonstrateCopyOnWriteArrayListSnapshotIteration();
        runConcurrentThroughputBenchmark();
    }

    private static void demonstrateSynchronizedMapContentionAndIteration() {
        System.out.println("\n--- 1. Collections.synchronizedMap() Traps & Iteration ---");
        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        syncMap.put("node-1", 100);
        syncMap.put("node-2", 200);

        System.out.println("SynchronizedMap created. Iterating requires explicit synchronized(map) block!");

        // Correct way to iterate synchronizedMap:
        synchronized (syncMap) {
            Iterator<Map.Entry<String, Integer>> it = syncMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                System.out.println("  Safe Sync Iteration -> Key: " + entry.getKey() + ", Val: " + entry.getValue());
            }
        }
        System.out.println("⚠️ Warning: Without synchronized(syncMap) during iteration, concurrent modification throws ConcurrentModificationException!");
    }

    private static void demonstrateConcurrentHashMapThroughputAndAtomicOps() {
        System.out.println("\n--- 2. ConcurrentHashMap: Fine-Grained Locking & Atomic Operations ---");
        ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();

        // Atomic computeIfAbsent (Lazy computation without external locking)
        cache.computeIfAbsent("config.timeout_ms", key -> {
            System.out.println("  Computing default timeout value atomically for " + key);
            return 5000;
        });

        // Second call uses cached result atomically
        cache.computeIfAbsent("config.timeout_ms", key -> 9999);
        System.out.println("  Computed Value in ConcurrentHashMap: " + cache.get("config.timeout_ms"));

        // Atomic putIfAbsent
        Integer existing = cache.putIfAbsent("config.timeout_ms", 10000);
        System.out.println("  putIfAbsent(10000) returned existing value: " + existing + " (Value un-modified: " + cache.get("config.timeout_ms") + ")");
    }

    private static void demonstrateCopyOnWriteArrayListSnapshotIteration() {
        System.out.println("\n--- 3. CopyOnWriteArrayList: Lock-Free Reads & Snapshot Iteration ---");
        CopyOnWriteArrayList<String> listeners = new CopyOnWriteArrayList<>();
        listeners.add("MetricsCollectorListener");
        listeners.add("AuditLoggerListener");

        System.out.println("Starting iteration over CopyOnWriteArrayList while concurrently modifying list...");

        // Iterating over list snapshot
        for (String listener : listeners) {
            System.out.println("  Iterating listener: " + listener);
            if (listener.equals("MetricsCollectorListener")) {
                // Concurrent modification during active iteration!
                listeners.add("SecurityTraceListener");
                System.out.println("  [Thread] Added 'SecurityTraceListener' during active iteration.");
            }
        }

        System.out.println("Iteration completed cleanly without ConcurrentModificationException!");
        System.out.println("Final list state after modification: " + listeners);
        System.out.println("💡 SRE Rule: Use CopyOnWriteArrayList ONLY when reads VASTLY outnumber writes (99% reads / 1% writes).");
    }

    private static void runConcurrentThroughputBenchmark() {
        System.out.println("\n--- 4. Concurrency Throughput Benchmark: SynchronizedMap vs ConcurrentHashMap ---");
        int threadCount = 10;
        int operationsPerThread = 50_000;

        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();

        long syncMapDuration = executeConcurrentWrites(syncMap, threadCount, operationsPerThread);
        long chmDuration = executeConcurrentWrites(concurrentMap, threadCount, operationsPerThread);

        System.out.println("Total Concurrent Write Operations: " + (threadCount * operationsPerThread));
        System.out.printf("  - Collections.synchronizedMap() : %d ms (High Mutex Contention)\n", syncMapDuration);
        System.out.printf("  - ConcurrentHashMap              : %d ms (Fine-Grained CAS/Bucket Lock)\n", chmDuration);
        System.out.printf("🚀 ConcurrentHashMap Speedup Ratio : %.2fx faster!\n", (double) syncMapDuration / chmDuration);
    }

    private static long executeConcurrentWrites(Map<String, Integer> map, int threadCount, int opsPerThread) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        map.put("key-" + (i % 100), threadId * 1000 + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        return System.currentTimeMillis() - startTime;
    }
}
