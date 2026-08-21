package com.example.generics;

/**
 * Main Runner Class for Phase 3.3: Generics & Type Safety Deep-Dive.
 * 
 * Executes comprehensive demonstrations covering:
 * - 3.3.1 Generic Classes, Interfaces, and Methods (<T>, <K, V>).
 * - 3.3.2 Bounded Types (<T extends Comparable<T>>, <T extends Number & Comparable<T>>).
 * - 3.3.3 Wildcards & PECS Principle (Unbounded ?, Upper ? extends Number, Lower ? super Integer).
 * - 3.3.4 Type Erasure, Synthetic Bridge Methods, Heap Pollution & Generic Array creation.
 */
public class GenericsMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 3.3: JAVA GENERICS & TYPE SAFETY DEEP-DIVE DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Generic Classes, Interfaces, and Methods
        GenericBasicsDemo.runDemo();

        // 2. Bounded Types (<T extends Comparable<T>>, Multiple Bounds)
        BoundedTypesDemo.runDemo();

        // 3. Wildcards & PECS Principle
        WildcardsPECSDemo.runDemo();

        // 4. Type Erasure, Bridge Methods, & Generic Warnings
        TypeErasureAndWarningsDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 3.3 GENERICS & TYPE SAFETY EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
