package com.example.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Section 6.3.1: Garbage Collection (GC) Algorithms & Phases.
 * 
 * Demonstrates:
 * - 1. Modern Garbage Collector Architectures: Serial, Parallel, G1GC, ZGC, Shenandoah.
 * - 2. Fundamental GC Execution Phases: Mark, Sweep, Compact & Stop-The-World (STW) pauses.
 * - 3. Programmatic GC Monitoring via GarbageCollectorMXBean (Count, Accumulation, Active Collector Names).
 * - 4. Senior SRE Insights for Garbage Collector selection based on SLA Latency vs Throughput.
 */
public class GarbageCollectionAlgorithmsDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.3.1 GARBAGE COLLECTION (GC) ALGORITHMS, PHASES & STW PAUSES");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Collector Comparison Matrix
        // ==========================================
        System.out.println("\n--- 1. Modern JVM Garbage Collector Architectures ---");
        System.out.println("   [SERIAL GC]     -XX:+UseSerialGC     | Single-threaded STW collector. Best for single-core / lightweight micro-containers (<512MB).");
        System.out.println("   [PARALLEL GC]   -XX:+UseParallelGC   | Multi-threaded throughput collector. Maximizes batch CPU throughput; pauses scale with heap.");
        System.out.println("   [G1GC]          -XX:+UseG1GC         | Region-based default collector (Java 9+). Predictable latency (-XX:MaxGCPauseMillis=200).");
        System.out.println("   [ZGC]           -XX:+UseZGC          | Scalable low-latency (<1ms STW pauses) using colored pointers & load barriers (16TB+ heap).");
        System.out.println("   [SHENANDOAH]    -XX:+UseShenandoahGC | Low-pause collector executing concurrent compaction alongside application threads.");

        // ==========================================
        // 2. GC Phases: Mark, Sweep, Compact
        // ==========================================
        System.out.println("\n--- 2. Core Garbage Collection Phases ---");
        System.out.println("   [PHASE 1: MARK]    Traverses object reference tree starting from 'GC Roots' (Stack frames, static fields, JNI references).");
        System.out.println("   [PHASE 2: SWEEP]   Reclaims memory bytes occupied by unreachable (dead) objects.");
        System.out.println("   [PHASE 3: COMPACT] Relocates surviving live objects contiguously, eliminating heap memory fragmentation.");
        System.out.println("   [STW PAUSE]        Stop-The-World pause halts all application execution threads while JVM performs critical GC phases.");

        // ==========================================
        // 3. Programmatic Inspection via GarbageCollectorMXBean
        // ==========================================
        System.out.println("\n--- 3. Programmatic GarbageCollectorMXBean Diagnostics ---");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        System.out.println("   [ACTIVE GC COLLECTORS]");
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.printf("   ├── Name: %-28s | Count: %4d | Total Time: %4d ms | Memory Pools: %s\n",
                    gcBean.getName(),
                    gcBean.getCollectionCount(),
                    gcBean.getCollectionTime(),
                    String.join(", ", gcBean.getMemoryPoolNames()));
        }

        // ==========================================
        // 4. Memory Allocation & GC Monitoring Trigger
        // ==========================================
        System.out.println("\n--- 4. Memory Allocation GC Activity Simulation ---");
        long startGcCount = getSystemGcCount(gcBeans);
        long startGcTime = getSystemGcTime(gcBeans);

        System.out.println("   Allocating and discarding 15 MB short-lived objects...");
        for (int i = 0; i < 30; i++) {
            byte[] temp = new byte[512 * 1024]; // 512 KB transient allocations
            temp[0] = (byte) (i & 0xFF);
        }

        System.out.println("   Requesting explicit System.gc() suggestion...");
        System.gc(); // Explicit suggestion to JVM

        try {
            Thread.sleep(100); // Allow GC worker threads to process
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endGcCount = getSystemGcCount(gcBeans);
        long endGcTime = getSystemGcTime(gcBeans);

        System.out.printf("   [GC METRICS AFTER ALLOCATION] Delta Collections: %d | Delta Pause Time: %d ms\n",
                (endGcCount - startGcCount), (endGcTime - startGcTime));

        System.out.println("\n   💡 SRE Golden Rule: Never call System.gc() in production code! Use -XX:+DisableExplicitGC to prevent manual STW triggers by legacy third-party libraries.");
    }

    private static long getSystemGcCount(List<GarbageCollectorMXBean> gcBeans) {
        long count = 0;
        for (GarbageCollectorMXBean bean : gcBeans) {
            long c = bean.getCollectionCount();
            if (c > 0) count += c;
        }
        return count;
    }

    private static long getSystemGcTime(List<GarbageCollectorMXBean> gcBeans) {
        long time = 0;
        for (GarbageCollectorMXBean bean : gcBeans) {
            long t = bean.getCollectionTime();
            if (t > 0) time += t;
        }
        return time;
    }
}
