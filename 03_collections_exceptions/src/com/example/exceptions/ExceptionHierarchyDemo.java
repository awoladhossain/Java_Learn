package com.example.exceptions;

import java.io.IOException;

/**
 * Phase 3.1 Demonstration: Exception Hierarchy
 * 
 * Hierarchy:
 *                      java.lang.Throwable
 *                               |
 *            +------------------+------------------+
 *            |                                     |
 *     java.lang.Error                       java.lang.Exception
 * (JVM/Hardware Failures)                          |
 * (Unchecked - Do Not Catch)       +---------------+---------------+
 *                                 |                               |
 *                       Checked Exceptions             java.lang.RuntimeException
 *                      (Compiler Enforced)                   (Unchecked)
 *                     (e.g., IOException)              (e.g., NullPointerException)
 */
public class ExceptionHierarchyDemo {

    public static void runDemo() {
        System.out.println("\n========================================================================");
        System.out.println("1️⃣  JAVA EXCEPTION HIERARCHY (Throwable -> Error vs Exception)");
        System.out.println("========================================================================");

        demonstrateThrowableClasses();
        demonstrateErrorVsException();
        demonstrateCheckedVsUnchecked();
        printSreHierarchyInsights();
    }

    private static void demonstrateThrowableClasses() {
        System.out.println("\n   📍 1. Throwable Root Class:");
        System.out.println("      • java.lang.Throwable is the superclass of all errors and exceptions in Java.");
        System.out.println("      • Only Throwable objects (or subclasses) can be thrown by JVM or throw statement.");
        
        Exception ex = new Exception("Sample Exception");
        Error err = new OutOfMemoryError("Simulated Heap Exhaustion");

        System.out.println("      • ex instanceof Throwable? " + (ex instanceof Throwable));
        System.out.println("      • err instanceof Throwable? " + (err instanceof Throwable));
    }

    private static void demonstrateErrorVsException() {
        System.out.println("\n   📍 2. java.lang.Error vs java.lang.Exception:");
        System.out.println("      • Error: Fatal runtime conditions outside application control (e.g., StackOverflowError, OutOfMemoryError).");
        System.out.println("        🚨 Rule: DO NOT catch java.lang.Error in normal code! The JVM state may be corrupted.");
        System.out.println("      • Exception: Conditions that reasonable applications might want to catch and handle.");

        try {
            simulateStackOverflow(0);
        } catch (StackOverflowError e) {
            System.out.println("      ⚠️ Caught StackOverflowError for demo: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    private static void simulateStackOverflow(int depth) {
        if (depth > 5) {
            // Guard clause to prevent actual infinite recursion crash in demo
            throw new StackOverflowError("Deep recursion limit reached in demo simulation");
        }
        simulateStackOverflow(depth + 1);
    }

    private static void demonstrateCheckedVsUnchecked() {
        System.out.println("\n   📍 3. Checked Exceptions vs RuntimeExceptions (Unchecked):");
        System.out.println("      • Checked Exceptions (subclasses of Exception excluding RuntimeException):");
        System.out.println("        - Enforced by java compiler at build time.");
        System.out.println("        - Forces developer to handle (try-catch) or declare (throws).");
        System.out.println("        - Used for predictable, recoverable external failures (e.g., File I/O, DB Connection).");

        System.out.println("      • Unchecked Exceptions (subclasses of RuntimeException):");
        System.out.println("        - NOT checked at compile time by Java compiler.");
        System.out.println("        - Usually indicate programming defects (e.g., NullPointerException, IndexOutOfBoundsException).");

        // Invoking checked method requiring explicit handling
        try {
            readConfigurationFile("missing_config.json");
        } catch (IOException e) {
            System.out.println("      [Checked Catch] Caught forced checked exception: " + e.getMessage());
        }

        // Invoking unchecked method
        try {
            validateUserInput(null);
        } catch (NullPointerException e) {
            System.out.println("      [Unchecked Catch] Caught unchecked exception: " + e.getMessage());
        }
    }

    private static void readConfigurationFile(String path) throws IOException {
        throw new IOException("Unable to open file stream at path: " + path);
    }

    private static void validateUserInput(String input) {
        if (input == null) {
            throw new NullPointerException("Input string parameters cannot be null!");
        }
    }

    private static void printSreHierarchyInsights() {
        System.out.println("\n   💡 SENIOR SRE HIERARCHY INSIGHTS:");
        System.out.println("      1. Never catch 'Throwable' or 'Error' in catch blocks! Catching Throwable swallows OutOfMemoryError");
        System.out.println("         and ThreadDeath, preventing JVM clean crash and producing zombie process state.");
        System.out.println("      2. Prefer Unchecked Exceptions for domain model exceptions in modern microservices to avoid");
        System.out.println("         polluting clean architectural interface method signatures with long 'throws' clauses.");
        System.out.println("      3. Use Checked Exceptions strictly when caller recovery is expected (e.g., fallback endpoints).");
    }
}
