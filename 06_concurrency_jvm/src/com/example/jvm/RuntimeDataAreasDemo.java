package com.example.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 6.2.2: JVM Runtime Data Areas.
 * 
 * Demonstrates:
 * - 1. Stack Area: Thread frame storage, local variables vs operand stack, controlled StackOverflowError.
 * - 2. Heap Memory: Young Generation (Eden, S0, S1) & Old Generation (Tenured), MemoryPoolMXBean inspection.
 * - 3. Metaspace: Native memory for class metadata (replacing PermGen), dynamic proxy footprint analysis, OOM: Metaspace cause.
 * - 4. Program Counter (PC) Registers & Native Method Stacks.
 */
public class RuntimeDataAreasDemo {

    private static int stackDepthCount = 0;

    /**
     * Interface used for dynamic Proxy class metadata allocation in Metaspace.
     */
    public interface DynamicTask {
        void executeTask();
    }

    /**
     * Controlled recursive method to demonstrate Stack Frame allocation and StackOverflowError.
     */
    private static void recursiveStackAllocation(long arg1, double arg2, Object arg3) {
        stackDepthCount++;
        // Create local variables inside each stack frame to consume frame memory
        long var1 = stackDepthCount * 10L;
        double var2 = stackDepthCount * 2.5;
        String var3 = "Stack Frame #" + stackDepthCount;

        // Recursive call without base case to cause stack overflow
        recursiveStackAllocation(var1, var2, var3);
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.2.2 JVM RUNTIME DATA AREAS (STACK, HEAP, METASPACE, PC & NATIVE STACK)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Stack Area & StackOverflowError
        // ==========================================
        System.out.println("\n--- 1. Stack Area & Stack Frame Mechanics ---");
        System.out.println("   [STACK FRAME COMPONENTS]");
        System.out.println("   ├── Local Variable Array (Primitive values & object references)");
        System.out.println("   ├── Operand Stack (Workspace for push/pop bytecode instructions)");
        System.out.println("   └── Frame Data (Constant pool resolution & Exception return table)");

        try {
            stackDepthCount = 0;
            recursiveStackAllocation(100L, 50.5, "RootFrame");
        } catch (StackOverflowError e) {
            System.out.println("   [CAUGHT] java.lang.StackOverflowError!");
            System.out.println("   [METRICS] Maximum stack call depth reached before overflow: " + stackDepthCount + " frames.");
            System.out.println("   💡 SRE Insight: Thread stack size is governed by -Xss (default ~1MB per thread). Deep recursive calls or recursive JSON parsing without depth guards will crash the thread stack.");
        }

        // ==========================================
        // 2. Heap Memory Breakdown & MemoryPoolMXBean
        // ==========================================
        System.out.println("\n--- 2. Heap Memory Generation Breakdown ---");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        System.out.printf("   [HEAP OVERALL] Init: %.2f MB | Used: %.2f MB | Committed: %.2f MB | Max: %.2f MB\n",
                heapUsage.getInit() / 1024.0 / 1024.0,
                heapUsage.getUsed() / 1024.0 / 1024.0,
                heapUsage.getCommitted() / 1024.0 / 1024.0,
                heapUsage.getMax() / 1024.0 / 1024.0);

        System.out.println("\n   [DETAILED HEAP MEMORY POOLS]");
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getType() == java.lang.management.MemoryType.HEAP) {
                MemoryUsage usage = pool.getUsage();
                System.out.printf("   ├── Pool: %-25s | Used: %6.2f MB | Committed: %6.2f MB | Max: %6.2f MB\n",
                        pool.getName(),
                        usage.getUsed() / 1024.0 / 1024.0,
                        usage.getCommitted() / 1024.0 / 1024.0,
                        usage.getMax() < 0 ? -1.0 : usage.getMax() / 1024.0 / 1024.0);
            }
        }

        System.out.println("\n   [ALLOCATION SIMULATION] Allocating objects to trigger Young Gen / Survivor behavior...");
        List<byte[]> heapAllocations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            heapAllocations.add(new byte[2 * 1024 * 1024]); // 2 MB blocks
        }
        System.out.println("   Allocated 10 MB in transient arrays. Garbage Collection will migrate surviving long-lived objects from Eden -> S0/S1 -> Old Gen (Tenured).");
        heapAllocations.clear(); // Eligible for GC

        // ==========================================
        // 3. Metaspace & Dynamic Class Metadata
        // ==========================================
        System.out.println("\n--- 3. Metaspace (Native Memory for Class Metadata) ---");
        System.out.println("   Metaspace replaced PermGen in Java 8. It lives in native OS memory and auto-grows up to -XX:MaxMetaspaceSize.");

        System.out.println("\n   [NON-HEAP MEMORY POOLS (METASPACE)]");
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getType() == java.lang.management.MemoryType.NON_HEAP) {
                MemoryUsage usage = pool.getUsage();
                System.out.printf("   ├── Pool: %-25s | Used: %6.2f MB | Committed: %6.2f MB | Max: %6.2f MB\n",
                        pool.getName(),
                        usage.getUsed() / 1024.0 / 1024.0,
                        usage.getCommitted() / 1024.0 / 1024.0,
                        usage.getMax() < 0 ? -1.0 : usage.getMax() / 1024.0 / 1024.0);
            }
        }

        System.out.println("\n   [DYNAMIC PROXY CLASS GENERATION] Generating 500 dynamic class metadata definitions...");
        List<Object> proxyList = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            InvocationHandler handler = (proxy, method, args) -> null;
            Object proxy = Proxy.newProxyInstance(
                    RuntimeDataAreasDemo.class.getClassLoader(),
                    new Class<?>[]{DynamicTask.class},
                    handler
            );
            proxyList.add(proxy);
        }
        System.out.println("   Dynamic proxies generated successfully. Metaspace stores method bytecode, constant pool tables, and class metadata.");
        System.out.println("   💡 SRE Insight: OutOfMemoryError: Metaspace occurs when dynamic class generation (e.g. CGLIB, ByteBuddy, reflection proxies) leaks ClassLoaders, preventing class metadata from being garbage collected.");

        // ==========================================
        // 4. PC Register & Native Method Stack
        // ==========================================
        System.out.println("\n--- 4. PC Registers & Native Method Stacks ---");
        System.out.println("   [PC REGISTER] Each thread has its own Program Counter (PC) Register pointing to the current executing JVM bytecode instruction address.");
        System.out.println("                 If executing a 'native' C/C++ method via JNI, the PC register value is undefined.");
        System.out.println("   [NATIVE METHOD STACK] Stores stack frames for Native C/C++ code executed via JNI (e.g., Unsafe memory allocation, POSIX file I/O).");
    }
}
