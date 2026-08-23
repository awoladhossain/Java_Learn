package com.example.jvm;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

/**
 * Section 6.2.3: JVM Execution Engine & JNI.
 * 
 * Demonstrates:
 * - 1. Interpreter vs JIT (Just-In-Time) Compiler (Tiered Compilation: C1 Client & C2 Server compilers).
 * - 2. Warmup Benchmark demonstrating performance transition from interpreted bytecode to JIT native machine code.
 * - 3. On-Stack Replacement (OSR): Compiling hot loops mid-execution directly on the stack.
 * - 4. CompilationMXBean JIT metrics inspection.
 * - 5. Native Method Interface (JNI) mechanics vs Modern Foreign Function & Memory (FFM) API (Java 21).
 */
public class ExecutionEngineDemo {

    /**
     * Compute intensive method used for JIT compilation warmup benchmarking.
     */
    private static long computeFibonacciLikeSequence(int iterations) {
        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += (i % 7 == 0) ? (i * 31L) : (i ^ 17L);
        }
        return result;
    }

    /**
     * Hot loop method to demonstrate On-Stack Replacement (OSR).
     */
    private static long performOsrHotLoop(int outerIterations, int innerIterations) {
        long sum = 0;
        for (int i = 0; i < outerIterations; i++) {
            for (int j = 0; j < innerIterations; j++) {
                sum += (i + j) & 0x0F;
            }
        }
        return sum;
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.2.3 EXECUTION ENGINE: INTERPRETER, JIT COMPILER (C1/C2), OSR & JNI");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. CompilationMXBean Metrics
        // ==========================================
        System.out.println("\n--- 1. JVM JIT Compiler Diagnostics ---");

        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        if (compilationMXBean != null) {
            System.out.println("   JIT Compiler Name           : " + compilationMXBean.getName());
            System.out.println("   Is Compilation Time Monitored: " + compilationMXBean.isCompilationTimeMonitoringSupported());
            if (compilationMXBean.isCompilationTimeMonitoringSupported()) {
                System.out.println("   Total Compilation Time (ms)  : " + compilationMXBean.getTotalCompilationTime() + " ms");
            }
        } else {
            System.out.println("   JIT Compiler in pure Interpreted Mode (-Xint).");
        }

        // ==========================================
        // 2. Interpreter vs JIT Compiler Warmup Benchmark
        // ==========================================
        System.out.println("\n--- 2. Interpreter vs JIT Compiler Warm-up Benchmark ---");
        System.out.println("   [TIERED COMPILATION PHASES]");
        System.out.println("   ├── Tier 0: Interpreter (Fast startup, low execution throughput)");
        System.out.println("   ├── Tier 1-3: C1 Compiler (Client JIT - Quick compilation, light profiling & inlining)");
        System.out.println("   └── Tier 4: C2 Compiler (Server JIT - Deep optimizations: escape analysis, loop unrolling, SIMD)");

        System.out.println("\n   [COLD INVOCATION - INTERPRETED / UN-WARMED]");
        long coldStartTime = System.nanoTime();
        long coldResult = computeFibonacciLikeSequence(10_000);
        long coldDuration = System.nanoTime() - coldStartTime;
        System.out.printf("   Cold Execution Duration : %,d ns (Result: %d)\n", coldDuration, coldResult);

        System.out.println("\n   [WARMING UP COMPILER] Executing method 20,000 times to cross C1/C2 compile threshold...");
        for (int i = 0; i < 20_000; i++) {
            computeFibonacciLikeSequence(10_000);
        }

        System.out.println("   [HOT INVOCATION - JIT C2 COMPILED NATIVE CODE]");
        long hotStartTime = System.nanoTime();
        long hotResult = computeFibonacciLikeSequence(10_000);
        long hotDuration = System.nanoTime() - hotStartTime;
        System.out.printf("   Hot Execution Duration  : %,d ns (Result: %d)\n", hotDuration, hotResult);

        double speedup = (double) coldDuration / Math.max(hotDuration, 1);
        System.out.printf("   🚀 Performance Speedup : %.2fx faster after JIT native machine code compilation!\n", speedup);

        // ==========================================
        // 3. On-Stack Replacement (OSR)
        // ==========================================
        System.out.println("\n--- 3. On-Stack Replacement (OSR) Mechanics ---");
        System.out.println("   OSR allows the JVM to replace the stack frame of a long-running hot loop mid-execution with JIT-compiled native code.");

        long osrStart = System.currentTimeMillis();
        long osrResult = performOsrHotLoop(100_000, 100);
        long osrElapsed = System.currentTimeMillis() - osrStart;

        System.out.printf("   OSR Hot Loop Completed 10,000,000 iterations in: %d ms (Checksum: %d)\n", osrElapsed, osrResult);
        System.out.println("   💡 SRE Insight: Warmup periods are critical for latency-sensitive microservices. Cold instances exhibit high p99 latency spikes until C2 JIT finishes compiling hot paths.");

        // ==========================================
        // 4. Native Method Interface (JNI) & FFM API
        // ==========================================
        System.out.println("\n--- 4. Native Method Interface (JNI) vs Java 21 FFM API ---");
        System.out.println("   [JNI MECHANICS]");
        System.out.println("   ├── Java code declares methods with 'native' keyword (e.g. Thread.currentThread(), System.currentTimeMillis()).");
        System.out.println("   ├── JVM uses JNI glue code to invoke native C/C++ compiled dynamic libraries (.so / .dll).");
        System.out.println("   └── Drawbacks: JNI calls cross JVM boundary, require C headers, and prevent JIT optimization across boundary.");
        
        System.out.println("\n   [MODERN FFM API (Java 21+)]");
        System.out.println("   ├── java.lang.foreign.Arena & MemorySegment provide safe, off-heap native memory access.");
        System.out.println("   └── Linker API allows invoking native C functions directly from Java code without writing JNI C code.");
    }
}
