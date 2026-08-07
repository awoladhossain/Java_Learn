package com.example;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Phase 1.2: Data Types, Stack vs Heap Memory Allocation
 * 
 * Topics Covered:
 * 1. All 8 Primitive Data Types & Ranges
 * 2. Reference Types (Classes, Arrays, Interfaces)
 * 3. Stack Frame Allocation vs Heap Object Allocation Mechanics
 * 4. Type Casting: Implicit (Widening) vs Explicit (Narrowing)
 * 5. Overflow, Underflow & Floating-Point Precision Hazards
 * 6. Senior SRE Insights: Memory Footprint & GC Impact (Primitives vs Boxed Objects)
 */
public class DataTypesAndMemoryDemo {

    // Domain sample class to demonstrate Heap Object allocation
    static class Account {
        private final long id;
        private double balance;

        public Account(long id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        public long getId() { return id; }
        public double getBalance() { return balance; }
    }

    // Domain sample interface to demonstrate Interface reference types
    interface Identifiable {
        String getEntityId();
    }

    static class ServiceNode implements Identifiable {
        private final String nodeId;

        public ServiceNode(String nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public String getEntityId() {
            return nodeId;
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 1.2: Data Types, Stack vs Heap Memory Allocation");
        System.out.println("========================================================================");

        demonstratePrimitiveTypes();
        demonstrateReferenceTypesAndMemory();
        demonstrateTypeCastingAndOverflow();
        demonstrateSreMemoryInsights();

        System.out.println("\n========================================================================");
        System.out.println("✅ Phase 1.2 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Primitives Deep Dive: byte, short, int, long, float, double, char, boolean
     */
    private static void demonstratePrimitiveTypes() {
        System.out.println("\n1️⃣  PRIMITIVE DATA TYPES IN JAVA (Stored directly in Stack frame when local):");
        System.out.println("------------------------------------------------------------------------");
        
        byte byteVal = 127;                      // 8-bit signed (-128 to 127)
        short shortVal = 32767;                  // 16-bit signed (-32,768 to 32,767)
        int intVal = 2147483647;                 // 32-bit signed (-2^31 to 2^31 - 1)
        long longVal = 9223372036854775807L;     // 64-bit signed (-2^63 to 2^63 - 1)
        float floatVal = 3.1415927f;             // 32-bit IEEE 754 Single Precision
        double doubleVal = 3.141592653589793;    // 64-bit IEEE 754 Double Precision
        char charVal = 'A';                      // 16-bit Unicode character ('\u0000' to '\uffff')
        boolean boolVal = true;                  // Logical true/false

        System.out.printf("   • byte    : %-22d | Size: %d bit (%d byte)  | Range: [%d to %d]%n",
                byteVal, Byte.SIZE, Byte.BYTES, Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.out.printf("   • short   : %-22d | Size: %d bit (%d bytes) | Range: [%d to %d]%n",
                shortVal, Short.SIZE, Short.BYTES, Short.MIN_VALUE, Short.MAX_VALUE);
        System.out.printf("   • int     : %-22d | Size: %d bit (%d bytes) | Range: [%d to %d]%n",
                intVal, Integer.SIZE, Integer.BYTES, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.printf("   • long    : %-22d | Size: %d bit (%d bytes) | Range: [%d to %d]%n",
                longVal, Long.SIZE, Long.BYTES, Long.MIN_VALUE, Long.MAX_VALUE);
        System.out.printf("   • float   : %-22f | Size: %d bit (%d bytes) | Min Non-Zero: %e%n",
                floatVal, Float.SIZE, Float.BYTES, Float.MIN_VALUE);
        System.out.printf("   • double  : %-22f | Size: %d bit (%d bytes) | Min Non-Zero: %e%n",
                doubleVal, Double.SIZE, Double.BYTES, Double.MIN_VALUE);
        System.out.printf("   • char    : '%c' (Unicode: U+%04X)   | Size: %d bit (%d bytes) | Range: [0 to %d]%n",
                charVal, (int) charVal, Character.SIZE, Character.BYTES, (int) Character.MAX_VALUE);
        System.out.printf("   • boolean : %-22b | Size: ~1 byte JVM dependent (stack/array encoding)%n",
                boolVal);
    }

    /**
     * 2️⃣ Reference Types & Stack vs Heap Allocation Dynamics
     */
    private static void demonstrateReferenceTypesAndMemory() {
        System.out.println("\n2️⃣  REFERENCE TYPES & STACK vs HEAP ALLOCATION:");
        System.out.println("------------------------------------------------------------------------");
        
        // Stack Frame Allocation:
        // 'primitiveVal' stores the actual value (100) inside the main thread's Stack Frame.
        int primitiveVal = 100;

        // Heap Allocation (Class Type Reference):
        // 'accRef' (reference variable) resides in the Stack Frame (~8 bytes pointer).
        // 'new Account(...)' creates the actual object memory layout in Heap space.
        Account accRef = new Account(1001L, 5499.50);

        // Heap Allocation (Array Reference):
        // 'numbers' reference resides on Stack, actual array elements live on Heap as an object.
        int[] numbers = new int[]{10, 20, 30, 40, 50};

        // Heap Allocation (Interface Reference):
        // 'nodeRef' stack reference points to a ServiceNode instance allocated on Heap.
        Identifiable nodeRef = new ServiceNode("node-us-east-1a");

        System.out.println("   📍 Stack Frame Variable Layout:");
        System.out.println("      - Primitive 'primitiveVal' = " + primitiveVal + " [Value stored directly in Stack]");
        System.out.println("      - Reference 'accRef'       = Pointer to Heap address");
        System.out.println("      - Reference 'numbers'      = Pointer to Array object on Heap");
        System.out.println("      - Reference 'nodeRef'      = Pointer to Interface implementation on Heap");

        System.out.println("\n   📍 Heap Object Inspection (Identity HashCodes & Metadata):");
        System.out.printf("      - Account Object  -> IdentityHashCode: 0x%x | Balance: $%.2f%n",
                System.identityHashCode(accRef), accRef.getBalance());
        System.out.printf("      - Array Object    -> IdentityHashCode: 0x%x | Length: %d%n",
                System.identityHashCode(numbers), numbers.length);
        System.out.printf("      - Node Object     -> IdentityHashCode: 0x%x | Node ID: %s%n",
                System.identityHashCode(nodeRef), nodeRef.getEntityId());
    }

    /**
     * 3️⃣ Type Casting: Implicit (Widening) vs Explicit (Narrowing) & Overflow/Underflow Hazards
     */
    private static void demonstrateTypeCastingAndOverflow() {
        System.out.println("\n3️⃣  TYPE CASTING & OVERFLOW / UNDERFLOW HAZARDS:");
        System.out.println("------------------------------------------------------------------------");

        // A. Implicit Casting (Widening): Safe conversion from smaller to larger domain type
        byte smallByte = 42;
        int widenedInt = smallByte;       // byte -> int (Automatic widening)
        double widenedDouble = widenedInt; // int -> double (Automatic widening)
        System.out.println("   A. Implicit (Widening) Casting [No Data Loss]:");
        System.out.println("      byte (" + smallByte + ") -> int (" + widenedInt + ") -> double (" + widenedDouble + ")");

        // B. Explicit Casting (Narrowing): Requires cast operator; potential truncation/overflow
        double doublePrice = 99.99;
        int truncatedPrice = (int) doublePrice; // Truncates decimal portion
        System.out.println("\n   B. Explicit (Narrowing) Casting [Truncation Hazard]:");
        System.out.println("      double (" + doublePrice + ") cast to int -> " + truncatedPrice + " (Decimal lost!)");

        // C. Explicit Casting & Overflow Hazard (Two's Complement Wrapping)
        int largeIntValue = 130;
        byte overflowByte = (byte) largeIntValue; // Range is -128 to 127
        System.out.println("\n   C. Explicit Cast Overflow Hazard:");
        System.out.println("      int (" + largeIntValue + ") cast to byte -> " + overflowByte
                + " (Reason: 130 binary 0000...10000010 wrapped in 8-bit 2's complement)");

        // D. Arithmetic Overflow (Exceeding Type Capacity)
        int maxInt = Integer.MAX_VALUE;
        int overflowedInt = maxInt + 1;
        System.out.println("\n   D. Integer Arithmetic Overflow:");
        System.out.println("      Integer.MAX_VALUE      =  " + maxInt);
        System.out.println("      Integer.MAX_VALUE + 1  = " + overflowedInt + " (Wrapped to Integer.MIN_VALUE)");

        // E. Floating-Point Precision Loss Hazard
        float f1 = 123456789.0f;
        float f2 = 123456790.0f;
        System.out.println("\n   E. Floating-Point Precision Loss Hazard (IEEE 754 24-bit mantissa):");
        System.out.println("      f1 (123456789.0f) = " + String.format("%.1f", f1));
        System.out.println("      f2 (123456790.0f) = " + String.format("%.1f", f2));
        System.out.println("      f1 == f2 ? " + (f1 == f2) + " (Precision loss causes silent equality trap in floats!)");

        double decimalSum = 0.1 + 0.2;
        System.out.println("\n   F. Binary Floating Point Precision Trap:");
        System.out.println("      0.1 + 0.2 = " + decimalSum + " (Use BigDecimal for financial precision in SRE services!)");
    }

    /**
     * 4️⃣ Senior SRE Memory Insights: Primitives vs Boxed Objects & Heap Impact
     */
    private static void demonstrateSreMemoryInsights() {
        System.out.println("\n4️⃣  SENIOR SRE MEMORY INSIGHTS & GC IMPACT:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Primitive vs Boxed Object Overhead:");
        System.out.println("      • Primitive 'int'           : 4 bytes of raw memory.");
        System.out.println("      • Boxed Object 'Integer'    : 16 - 24 bytes (Mark Word 8B + Klass Word 4B + Value 4B + Alignment padding).");
        System.out.println("      • Memory Amplification      : ~4x to 6x overhead per number when using Wrapper Objects!");

        System.out.println("\n   💡 Stack Frame vs Heap Life Cycle:");
        System.out.println("      • Stack Frames: Automatically allocated and deallocated when method call finishes (Zero GC cost).");
        System.out.println("      • Heap Objects: Must be collected by Garbage Collection (G1GC/ZGC). High object allocation rates cause GC STW pauses.");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / 1024;
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed() / 1024;

        System.out.println("\n   📊 Live Memory Statistics (Current Process):");
        System.out.printf("      • Heap Used     : %d KB%n", heapUsed);
        System.out.printf("      • Metaspace/Non-Heap: %d KB%n", nonHeapUsed);
    }
}
