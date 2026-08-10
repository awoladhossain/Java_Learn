package com.example;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;

/**
 * Phase 1.4: String Handling & Memory Optimization
 * 
 * Topics Covered:
 * 1. String Immutability & String Constant Pool (SCP) dynamics in Heap Memory
 * 2. Literal vs Object instantiation and String.intern() mechanics
 * 3. Performance & Allocation Comparison: String vs StringBuilder vs StringBuffer
 * 4. Single & Multi-Dimensional (Ragged) Arrays Memory Layout & Mechanics
 * 5. System.arraycopy vs Arrays utilities for high-throughput data manipulation
 * 6. Senior SRE Insights: GC Pressure from String Storms, Compact Strings (Java 9+), & Memory Footprints
 */
public class StringAndArrayMemoryDemo {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 1.4: String Handling & Memory Optimization Deep Dive");
        System.out.println("========================================================================");

        demonstrateStringImmutabilityAndScp();
        demonstrateStringVsBuildersBenchmark();
        demonstrateArrayMemoryLayoutAndUtilities();
        demonstrateSreStringAndMemoryInsights();

        System.out.println("\n========================================================================");
        System.out.println("✅ Phase 1.4 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ String Immutability & String Constant Pool (SCP) Mechanics
     */
    private static void demonstrateStringImmutabilityAndScp() {
        System.out.println("\n1️⃣  STRING IMMUTABILITY & STRING CONSTANT POOL (SCP):");
        System.out.println("------------------------------------------------------------------------");

        // A. Literal Allocation vs 'new String()' Heap Allocation
        // Literals are stored in the String Constant Pool (SCP) within Heap memory.
        String str1 = "JavaSRE";
        String str2 = "JavaSRE"; // Reuses existing object in SCP

        // 'new' keyword creates a NEW object on Heap, outside SCP (referencing the pool character data)
        String str3 = new String("JavaSRE");
        String str4 = str3.intern(); // Explicitly adds/retrieves from SCP

        System.out.println("   📍 Reference Equality (==) vs Logical Equality (.equals()):");
        System.out.println("      • str1 (\"JavaSRE\") == str2 (\"JavaSRE\")   : " + (str1 == str2) + " (Same SCP Object reference)");
        System.out.println("      • str1 (\"JavaSRE\") == str3 (new String) : " + (str1 == str3) + " (Different Heap Object address)");
        System.out.println("      • str1.equals(str3)                      : " + str1.equals(str3) + " (Identical character sequence)");
        System.out.println("      • str1 == str3.intern()                  : " + (str1 == str4) + " (intern() returns SCP reference)");

        System.out.printf("%n   📍 Memory Identity HashCodes (Heap Address Identifiers):%n");
        System.out.printf("      - str1 (SCP Literal)  -> IdentityHashCode: 0x%x%n", System.identityHashCode(str1));
        System.out.printf("      - str2 (SCP Literal)  -> IdentityHashCode: 0x%x%n", System.identityHashCode(str2));
        System.out.printf("      - str3 (Heap Object)  -> IdentityHashCode: 0x%x%n", System.identityHashCode(str3));
        System.out.printf("      - str4 (Interned SCP) -> IdentityHashCode: 0x%x%n", System.identityHashCode(str4));

        // B. Proof of String Immutability
        System.out.println("\n   📍 Proof of Immutability:");
        String original = "Production";
        int originalHashCode = System.identityHashCode(original);

        String modified = original.concat("-Service");
        int modifiedHashCode = System.identityHashCode(modified);

        System.out.println("      • original string              : \"" + original + "\" (IdentityHashCode: 0x" + Integer.toHexString(originalHashCode) + ")");
        System.out.println("      • original.concat(\"-Service\")   : \"" + modified + "\" (IdentityHashCode: 0x" + Integer.toHexString(modifiedHashCode) + ")");
        System.out.println("      • Immutability Verification    : Original object was NOT mutated; a NEW object was created on Heap.");
    }

    /**
     * 2️⃣ Performance Benchmark: String (+) vs StringBuilder vs StringBuffer
     */
    private static void demonstrateStringVsBuildersBenchmark() {
        System.out.println("\n2️⃣  STRING vs STRINGBUILDER vs STRINGBUFFER ALLOCATION BENCHMARK:");
        System.out.println("------------------------------------------------------------------------");

        int iterations = 30_000;
        System.out.println("   📍 Executing " + iterations + " String Append Operations across constructs:");

        // A. String Concatenation (+) in Loop (Creates temp StringBuilder + String on each iteration!)
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long startHeap = memBean.getHeapMemoryUsage().getUsed();
        long startTime = System.nanoTime();

        String strResult = "";
        for (int i = 0; i < iterations; i++) {
            strResult += "x";
        }
        long durationStringMs = (System.nanoTime() - startTime) / 1_000_000;
        long stringHeapUsedKb = Math.max(0, (memBean.getHeapMemoryUsage().getUsed() - startHeap) / 1024);

        // Force GC hint to normalize memory comparison for next runs
        System.gc();

        // B. StringBuilder (Unsynchronized, single-threaded buffer)
        startTime = System.nanoTime();
        StringBuilder sbResult = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sbResult.append("x");
        }
        String sbFinal = sbResult.toString();
        long durationSbMs = (System.nanoTime() - startTime) / 1_000_000;

        // C. StringBuffer (Synchronized, thread-safe buffer)
        startTime = System.nanoTime();
        StringBuffer sbfResult = new StringBuffer();
        for (int i = 0; i < iterations; i++) {
            sbfResult.append("x");
        }
        String sbfFinal = sbfResult.toString();
        long durationSbfMs = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("\n   📊 Benchmark Results:");
        System.out.printf("      • String (+) Loop           : %4d ms | Length: %d chars | Heap Alloc Delta: ~%d KB | High GC Pressure!%n",
                durationStringMs, strResult.length(), stringHeapUsedKb);
        System.out.printf("      • StringBuilder (Unsynced)  : %4d ms | Length: %d chars | Fast & Efficient!%n", durationSbMs, sbFinal.length());
        System.out.printf("      • StringBuffer  (Synced)    : %4d ms | Length: %d chars | Intrinsic Lock Overhead%n", durationSbfMs, sbfFinal.length());

        if (durationStringMs > 0 && durationSbMs >= 0) {
            double speedup = (double) durationStringMs / Math.max(1, durationSbMs);
            System.out.printf("%n   🚀 SRE Takeaway: StringBuilder is ~%.1fx FASTER than String '+' concatenation inside loops!%n", speedup);
        }
    }

    /**
     * 3️⃣ One-Dimensional & Multi-Dimensional (Jagged) Arrays
     */
    private static void demonstrateArrayMemoryLayoutAndUtilities() {
        System.out.println("\n3️⃣  ONE-DIMENSIONAL & MULTI-DIMENSIONAL ARRAYS:");
        System.out.println("------------------------------------------------------------------------");

        // A. 1D Primitive Array & Memory Mechanics
        int[] latencySamples = new int[]{45, 12, 89, 34, 120, 23};
        System.out.println("   📍 1D Primitive Array (Contiguous primitive memory layout on Heap):");
        System.out.println("      • Initial Array        : " + Arrays.toString(latencySamples));

        // Array Manipulation with java.util.Arrays
        Arrays.sort(latencySamples);
        System.out.println("      • Arrays.sort()        : " + Arrays.toString(latencySamples));
        int searchIdx = Arrays.binarySearch(latencySamples, 34);
        System.out.println("      • Binary Search (34)   : Found at index " + searchIdx);

        // High-Performance Native Array Copying: System.arraycopy
        int[] copiedSamples = new int[4];
        // System.arraycopy(src, srcPos, dest, destPos, length) - executed via low-level native memmove
        System.arraycopy(latencySamples, 0, copiedSamples, 0, 4);
        System.out.println("      • System.arraycopy()   : First 4 elements -> " + Arrays.toString(copiedSamples));

        // B. 2D / Jagged (Ragged) Arrays
        System.out.println("\n   📍 2D Multi-Dimensional Array (Array of Array References on Heap):");
        // Creating a Jagged Array (rows with variable column lengths)
        int[][] clusterMetrics = new int[3][];
        clusterMetrics[0] = new int[]{100, 102, 101};            // Region 1: 3 nodes
        clusterMetrics[1] = new int[]{95, 98};                   // Region 2: 2 nodes
        clusterMetrics[2] = new int[]{110, 115, 112, 108, 120};   // Region 3: 5 nodes

        System.out.println("      • Jagged Array Deep Printing (Arrays.deepToString):");
        System.out.println("        " + Arrays.deepToString(clusterMetrics));

        System.out.println("      • Iterating Jagged Array Grid:");
        for (int region = 0; region < clusterMetrics.length; region++) {
            System.out.printf("        - Region [%d] (%d nodes): ", region, clusterMetrics[region].length);
            for (int node = 0; node < clusterMetrics[region].length; node++) {
                System.out.print(clusterMetrics[region][node] + "ms ");
            }
            System.out.println();
        }

        // C. Structural Equality: Arrays.equals vs Arrays.deepEquals
        int[][] clusterMetricsCopy = new int[][]{
            {100, 102, 101},
            {95, 98},
            {110, 115, 112, 108, 120}
        };

        System.out.println("\n   📍 Multi-Dimensional Equality Mechanics:");
        System.out.println("      • Arrays.equals(grid1, grid2)    : " + Arrays.equals(clusterMetrics, clusterMetricsCopy) 
                + " (Compares outer array references only!)");
        System.out.println("      • Arrays.deepEquals(grid1, grid2): " + Arrays.deepEquals(clusterMetrics, clusterMetricsCopy) 
                + " (Recursively compares element values!)");
    }

    /**
     * 4️⃣ Senior SRE Insights & JVM Internals
     */
    private static void demonstrateSreStringAndMemoryInsights() {
        System.out.println("\n4️⃣  SENIOR SRE INSIGHTS & JVM STRING OPTIMIZATIONS:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Compact Strings (Java 9+ Optimization):");
        System.out.println("      • Prior to Java 9, String backed by 'char[]' (2 bytes per character, UTF-16).");
        System.out.println("      • Java 9+ uses 'byte[]' + coder byte flag (LATIN1 vs UTF16).");
        System.out.println("      • Single-byte (ASCII/Latin-1) strings reduce Heap usage by ~50% in enterprise applications!");

        System.out.println("\n   💡 Avoiding Allocation Storms & GC Pressure:");
        System.out.println("      • String concatenation in tight loops (e.g. log formatting or JSON generation)");
        System.out.println("        instantiates thousands of short-lived 'StringBuilder' and 'char[]/byte[]' objects in Eden Space.");
        System.out.println("      • This triggers frequent Young Generation Garbage Collection (STW pauses).");
        System.out.println("      • Solution: Pre-size StringBuilder capacity (e.g. 'new StringBuilder(1024)') or use SLF4J parametrized logging.");

        System.out.println("\n   💡 String.intern() Caution in SRE Systems:");
        System.out.println("      • String Constant Pool uses a fixed-size internal Hashtable (-XX:StringTableSize).");
        System.out.println("      • Interning millions of unique dynamic strings (e.g. UUIDs or request IDs) leads to bucket collisions,");
        System.out.println("        degrading intern() lookup performance to O(N) and consuming Metaspace/Native memory.");

        System.out.println("\n   💡 Array Allocation Memory Footprint:");
        System.out.println("      • Primitive 'int[1000]' occupies ~4,016 bytes (16B header + 4000B elements).");
        System.out.println("      • Object 'Integer[1000]' occupies ~24,016+ bytes (16B header + 4000B pointers + 1000 Integer objects on Heap!).");
        System.out.println("      • Prefer primitive arrays in high-throughput metrics pipelines to maintain cache locality and minimize GC load.");
    }
}
