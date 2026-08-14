package com.example.exceptions;

/**
 * Main Runner Class for Phase 3.1: Exception Handling & Fault Tolerance.
 * 
 * Executes comprehensive demonstrations covering:
 * - 3.1.1 Exception Hierarchy (Throwable -> Error vs Exception -> RuntimeException vs Checked)
 * - 3.1.2 try-catch-finally execution semantics & anti-pattern traps
 * - 3.1.3 try-with-resources & AutoCloseable resource leak prevention
 * - 3.1.4 Custom domain exceptions with explicit error codes
 * - 3.1.5 Exception chaining & suppressed exceptions
 */
public class ExceptionHandlingMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 3.1: EXCEPTION HANDLING & FAULT TOLERANCE DEMONSTRATION");
        System.out.println("========================================================================");

        // 1. Exception Hierarchy & Classification
        ExceptionHierarchyDemo.runDemo();

        // 2. try-catch-finally Execution Semantics & Anti-patterns
        TryCatchFinallySemanticsDemo.runDemo();

        // 3. try-with-resources & AutoCloseable Leak Prevention
        TryWithResourcesDemo.runDemo();

        // 4. Exception Chaining & Suppressed Exception Management
        ExceptionChainingAndSuppressionDemo.runDemo();

        System.out.println("\n========================================================================");
        System.out.println("✅ PHASE 3.1 EXCEPTION HANDLING DEMO EXECUTED SUCCESSFULLY!");
        System.out.println("========================================================================");
    }
}
