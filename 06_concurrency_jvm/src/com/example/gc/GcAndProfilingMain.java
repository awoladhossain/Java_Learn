package com.example.gc;

/**
 * Main Runner Class for Phase 6.3: Garbage Collection (GC) Algorithms & Memory Tuning.
 * 
 * Executes comprehensive demonstrations covering:
 * - 6.3.1 Garbage Collection Algorithms: Serial, Parallel, G1GC, ZGC, Shenandoah, Mark-Sweep-Compact phases & STW pauses.
 * - 6.3.2 Diagnosing OutOfMemoryError (OOM) types: Java heap space, GC overhead limit exceeded, Metaspace, Unable to create new native thread.
 * - 6.3.3 JVM Diagnostic Tools & Profiling: jcmd, jstat, jmap, jstack, Eclipse MAT, async-profiler & Flamegraphs.
 */
public class GcAndProfilingMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("🗑️  PHASE 6.3: GARBAGE COLLECTION (GC) ALGORITHMS & MEMORY TUNING DEMO");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Garbage Collection Algorithms, Phases & STW Pauses
        GarbageCollectionAlgorithmsDemo.runDemo();

        // 2. Diagnosing OutOfMemoryError (OOM) Types & Root Cause Remedies
        OomDiagnosisAndTypesDemo.runDemo();

        // 3. JVM CLI Diagnostics, Heap Dump Analysis & Async Profiling
        JvmDiagnosticsAndProfilingDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 6.3 GC ALGORITHMS & MEMORY TUNING EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
