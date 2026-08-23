package com.example.gc;

import java.lang.management.ManagementFactory;

/**
 * Section 6.3.3: JVM Diagnostic Tools, Heap Dump Analysis & Async Profiling.
 * 
 * Demonstrates:
 * - 1. JVM Diagnostic CLI Suite: jcmd, jstat -gcutil, jmap heap dumps, jstack thread dumps.
 * - 2. Heap Dump Analysis: Eclipse MAT, Shallow Size vs Retained Size, Dominator Tree & Leak Suspects.
 * - 3. CPU & Allocation Profiling: async-profiler, AsyncGetCallTrace, Safepoint Bias, and Flamegraph Generation.
 */
public class JvmDiagnosticsAndProfilingDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.3.3 JVM DIAGNOSTICS (CLI), HEAP DUMP ANALYSIS & ASYNC PROFILING");
        System.out.println("------------------------------------------------------------------------");

        long pid = ProcessHandle.current().pid();
        String jvmName = ManagementFactory.getRuntimeMXBean().getVmName();

        // ==========================================
        // 1. JVM CLI Diagnostic Tools
        // ==========================================
        System.out.println("\n--- 1. JVM CLI Diagnostic Tools (Current PID: " + pid + " | " + jvmName + ") ---");
        System.out.println("   [jcmd]   Multi-purpose JVM diagnostic command:");
        System.out.printf("            $ jcmd %d VM.flags\n", pid);
        System.out.printf("            $ jcmd %d GC.heap_info\n", pid);
        System.out.printf("            $ jcmd %d VM.metaspace\n", pid);

        System.out.println("\n   [jstat]  Real-time GC Statistics Monitor (Interval: 1000ms):");
        System.out.printf("            $ jstat -gcutil %d 1000\n", pid);
        System.out.println("            Columns: S0 (Survivor0), S1 (Survivor1), E (Eden), O (Old), M (Metaspace), YGC (Young GC Count), FGC (Full GC Count), GCT (Total GC Time)");

        System.out.println("\n   [jmap]   Heap Dump Generator:");
        System.out.printf("            $ jmap -dump:live,format=b,file=heap_%d.hprof %d\n", pid, pid);

        System.out.println("\n   [jstack] Thread Dump Analyzer:");
        System.out.printf("            $ jstack %d > thread_dump_%d.txt\n", pid, pid);

        // ==========================================
        // 2. Heap Dump Analysis (Eclipse MAT / VisualVM / JProfiler)
        // ==========================================
        System.out.println("\n--- 2. Heap Dump Analysis (Eclipse MAT Concepts) ---");
        System.out.println("   [SHALLOW SIZE]  Memory consumed by the object structure itself (Object header + primitive fields + reference pointer array).");
        System.out.println("   [RETAINED SIZE] Total memory freed if the object is garbage collected (Shallow size + transitive closure of objects uniquely referenced).");
        System.out.println("   [DOMINATOR TREE] Hierarchical graph view where node A dominates node B if every path from GC Root to B passes through A.");
        System.out.println("   [LEAK SUSPECTS] Automated MAT report pinpointing single objects or collections retaining massive proportions of total heap.");

        // ==========================================
        // 3. CPU & Allocation Profiling (async-profiler & Flamegraphs)
        // ==========================================
        System.out.println("\n--- 3. Low-Overhead Profiling: async-profiler & Flamegraphs ---");
        System.out.println("   [SAFEPOINT BIAS TRAP]");
        System.out.println("   Traditional profilers (e.g. VisualVM sampling) sample stacks only at JVM Safepoints, introducing severe measurement bias!");

        System.out.println("\n   [ASYNC-PROFILER]");
        System.out.println("   Uses AsyncGetCallTrace API and Linux perf_events to sample call stacks out-of-band without safepoint bias.");
        System.out.println("   ├── CPU Profiling        : $ ./asprof -d 30 -f cpu_flamegraph.html " + pid);
        System.out.println("   └── Allocation Profiling : $ ./asprof -e alloc -d 30 -f alloc_flamegraph.html " + pid);

        System.out.println("\n   [FLAMEGRAPH VISUALIZATION GUIDE]");
        System.out.println("   ├── X-Axis (Width) : Proportion of total samples spent in method (Wider box = higher CPU/Memory consumption).");
        System.out.println("   └── Y-Axis (Height): Stack frame depth (Top box = executing method, lower boxes = call stack parentage).");

        System.out.println("\n   💡 Senior SRE Production Profiling Rule: Run async-profiler allocation profiling (-e alloc) during performance testing to identify unnecessary object instantiations causing high GC pressure.");
    }
}
