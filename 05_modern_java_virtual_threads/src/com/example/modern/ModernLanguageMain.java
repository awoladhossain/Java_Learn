package com.example.modern;

/**
 * Main Runner Class for Phase 5.3: Modern Language Innovations.
 * 
 * Executes comprehensive demonstrations covering:
 * - 5.3.1 Optional<T> API (orElseGet, ifPresentOrElse, flatMap, stream, SRE best practices).
 * - 5.3.2 Local Variable Type Inference (var mechanics, rules & readability).
 * - 5.3.3 Pattern Matching & Switch Expressions (instanceof smart casting, switch expressions, record patterns, guarded when clauses).
 * - 5.3.4 Text Blocks (""" ... """, formatting, escape sequences, K8s YAML & SQL templates).
 */
public class ModernLanguageMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("⚡ PHASE 5.3: MODERN JAVA LANGUAGE INNOVATIONS DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Optional<T> API Deep-Dive
        OptionalApiDemo.runDemo();

        // 2. Local Variable Type Inference (var)
        VarTypeInferenceDemo.runDemo();

        // 3. Pattern Matching & Switch Expressions
        PatternMatchingDemo.runDemo();

        // 4. Text Blocks & String Formatting
        TextBlocksDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 5.3 MODERN LANGUAGE INNOVATIONS EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
