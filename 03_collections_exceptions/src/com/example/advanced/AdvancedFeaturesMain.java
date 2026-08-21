package com.example.advanced;

/**
 * Main Runner Class for Phase 3.4: Advanced Language Features.
 * 
 * Executes comprehensive demonstrations covering:
 * - 3.4.1 Enums with fields, constructors, and abstract methods.
 * - 3.4.2 Java Records (canonical & compact constructors, immutability & defensive copies).
 * - 3.4.3 Sealed Classes & Interfaces (sealed, permits, final, sealed, non-sealed, pattern matching).
 * - 3.4.4 Reflection API & Custom Annotations (@Retention, @Target, dynamic injection & invocation).
 */
public class AdvancedFeaturesMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 3.4: ADVANCED JAVA LANGUAGE FEATURES DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Enums Deep-Dive (Fields, Constructors, Abstract Methods, EnumSet, EnumMap)
        EnumsDeepDiveDemo.runDemo();

        // 2. Java Records (Canonical vs Compact Constructors, Validation, Defensive Copies)
        RecordsDeepDiveDemo.runDemo();

        // 3. Sealed Classes & Interfaces (Permitted Subtypes, Exhaustive Pattern Matching)
        SealedClassesDemo.runDemo();

        // 4. Reflection API & Custom Annotations (Dependency Injection & Method Invocation Engine)
        ReflectionAndAnnotationsDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 3.4 ADVANCED LANGUAGE FEATURES EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
