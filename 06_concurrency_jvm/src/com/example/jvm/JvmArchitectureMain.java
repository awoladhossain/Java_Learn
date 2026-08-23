package com.example.jvm;

/**
 * Main Runner Class for Phase 6.2: JVM Architecture & Internal Subsystems.
 * 
 * Executes comprehensive demonstrations covering:
 * - 6.2.1 Class Loader Subsystem: Loading, Linking (Verification, Preparation, Resolution), Initialization, Parent Delegation Model, Custom ClassLoader.
 * - 6.2.2 Runtime Data Areas: Stack Area & frames, Heap Generations (Young/Eden/S0/S1, Old Gen), Metaspace (Native Metadata), PC Registers, Native Method Stack.
 * - 6.2.3 Execution Engine & JNI: Interpreter vs JIT (C1/C2 compilers, On-Stack Replacement), CompilationMXBean diagnostics, JNI & Java 21 FFM API.
 */
public class JvmArchitectureMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("⚙️  PHASE 6.2: JVM ARCHITECTURE & INTERNAL SUBSYSTEMS DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Class Loader Subsystem & Parent Delegation Model
        ClassLoaderSubsystemDemo.runDemo();

        // 2. Runtime Data Areas (Stack, Heap, Metaspace, PC & Native Stacks)
        RuntimeDataAreasDemo.runDemo();

        // 3. Execution Engine (Interpreter vs JIT C1/C2, OSR, JNI & FFM API)
        ExecutionEngineDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 6.2 JVM ARCHITECTURE & SUBSYSTEMS EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
