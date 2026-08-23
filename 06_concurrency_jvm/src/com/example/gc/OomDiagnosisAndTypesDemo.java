package com.example.gc;

import java.util.ArrayList;
import java.util.List;

/**
 * Section 6.3.2: Diagnosing OutOfMemoryError (OOM) Types & Root Causes.
 * 
 * Demonstrates & Diagnoses:
 * - 1. java.lang.OutOfMemoryError: Java heap space
 * - 2. java.lang.OutOfMemoryError: GC overhead limit exceeded
 * - 3. java.lang.OutOfMemoryError: Metaspace
 * - 4. java.lang.OutOfMemoryError: Unable to create new native thread
 * 
 * SRE Troubleshooting Guide: Root Causes, Stack Signatures, Heap Dump Analysis & JVM Flags.
 */
public class OomDiagnosisAndTypesDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.3.2 DIAGNOSING OUTOFMEMORYERROR (OOM) TYPES & SRE REMEDIATION");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. OutOfMemoryError: Java heap space
        // ==========================================
        System.out.println("\n--- 1. java.lang.OutOfMemoryError: Java heap space ---");
        System.out.println("   [CAUSE]           Heap memory is completely full with live reachable objects.");
        System.out.println("   [ROOT CAUSE]      Memory leak (e.g. static Collections holding references) or heap size (-Xmx) is too small.");
        System.out.println("   [STACK SIGNATURE] java.lang.OutOfMemoryError: Java heap space");
        System.out.println("   [SRE REMEDIATION] 1. Enable automatic OOM heap dump: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/heap.hprof");
        System.out.println("                     2. Inspect Dominator Tree in Eclipse MAT / VisualVM to identify retaining GC Roots.");

        // ==========================================
        // 2. OutOfMemoryError: GC overhead limit exceeded
        // ==========================================
        System.out.println("\n--- 2. java.lang.OutOfMemoryError: GC overhead limit exceeded ---");
        System.out.println("   [CAUSE]           JVM is spending more than 98% of total CPU time executing GC and reclaiming < 2% of heap.");
        System.out.println("   [ROOT CAUSE]      Heap is nearly thrashing; GC is working constantly but recovering almost zero memory.");
        System.out.println("   [STACK SIGNATURE] java.lang.OutOfMemoryError: GC overhead limit exceeded");
        System.out.println("   [SRE REMEDIATION] Increase heap allocation (-Xmx) and fix high object allocation rate in tight loops.");

        // ==========================================
        // 3. OutOfMemoryError: Metaspace
        // ==========================================
        System.out.println("\n--- 3. java.lang.OutOfMemoryError: Metaspace ---");
        System.out.println("   [CAUSE]           Native memory reserved for class metadata (method bytecode, constant pool) is exhausted.");
        System.out.println("   [ROOT CAUSE]      Unloaded dynamic ClassLoaders, leaking dynamic proxies (CGLIB/ByteBuddy), or micro-frameworks.");
        System.out.println("   [STACK SIGNATURE] java.lang.OutOfMemoryError: Metaspace");
        System.out.println("   [SRE REMEDIATION] Set -XX:MaxMetaspaceSize=512m and inspect Metaspace footprint using 'jcmd <pid> VM.metaspace'.");

        // ==========================================
        // 4. OutOfMemoryError: Unable to create new native thread
        // ==========================================
        System.out.println("\n--- 4. java.lang.OutOfMemoryError: Unable to create new native thread ---");
        System.out.println("   [CAUSE]           JVM cannot request OS kernel to allocate native memory for a new OS thread stack frame.");
        System.out.println("   [ROOT CAUSE]      Thread leak (creating new Thread() instead of thread pool), thread stack size (-Xss) too large, or OS process limit reached.");
        System.out.println("   [STACK SIGNATURE] java.lang.OutOfMemoryError: Unable to create new native thread");
        System.out.println("   [SRE REMEDIATION] 1. Reduce thread stack size from default 1MB to -Xss256k.");
        System.out.println("                     2. Migrate to Java 21 Virtual Threads (Thread.ofVirtual()) for lightweight concurrency.");
        System.out.println("                     3. Increase Linux user process limit: 'ulimit -u 65535' or '/etc/security/limits.conf'.");

        // ==========================================
        // 5. Controlled Memory Leak Simulation Safeguard
        // ==========================================
        System.out.println("\n--- 5. Controlled Memory Leak Inspection Demonstration ---");
        System.out.println("   Simulating transient allocation pool for diagnostic verification...");

        List<byte[]> transientLeakPool = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            transientLeakPool.add(new byte[1 * 1024 * 1024]); // 1MB blocks
        }
        System.out.printf("   Allocated %d MB in transient leak pool.\n", transientLeakPool.size());
        transientLeakPool.clear(); // Reclaimed safely
        System.out.println("   Pool cleared safely. Zero OOM exceptions triggered during diagnostic run.");
    }
}
