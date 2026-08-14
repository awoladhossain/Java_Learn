package com.example.exceptions;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Phase 3.1 Demonstration: try-catch-finally Execution Semantics
 * 
 * Teaches:
 * 1. Sequential execution flow of try, catch, and finally.
 * 2. Order of multi-catch clauses (specific to general hierarchy).
 * 3. Java Multi-catch syntax (catch (TypeA | TypeB e)).
 * 4. Critical Gotcha: 'return' inside 'finally' block (overwrites return values & swallows exceptions!).
 * 5. Exception Swallowing when 'finally' throws an unhandled exception.
 * 6. System.exit(code) behavior bypassing 'finally'.
 */
public class TryCatchFinallySemanticsDemo {

    public static void runDemo() {
        System.out.println("\n========================================================================");
        System.out.println("2️⃣  TRY-CATCH-FINALLY EXECUTION SEMANTICS & GOTCHAS");
        System.out.println("========================================================================");

        demonstrateNormalExecutionFlow();
        demonstrateCatchOrderingAndMultiCatch();
        demonstrateReturnInFinallyAntiPattern();
        demonstrateExceptionSwallowingInFinally();
        demonstrateSystemExitBehavior();
    }

    private static void demonstrateNormalExecutionFlow() {
        System.out.println("\n   📍 1. Standard try-catch-finally Execution Order:");
        System.out.println("      Executing normal operations...");

        boolean finallyExecuted = false;
        try {
            System.out.println("      [TRY BLOCK] Executing work step 1...");
            int result = 10 / 2;
            System.out.println("      [TRY BLOCK] Step 1 finished with result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("      [CATCH BLOCK] Handled arithmetic error!");
        } finally {
            finallyExecuted = true;
            System.out.println("      [FINALLY BLOCK] Always executes for cleanup! (Executed = " + finallyExecuted + ")");
        }
    }

    private static void demonstrateCatchOrderingAndMultiCatch() {
        System.out.println("\n   📍 2. Multi-Catch & Catch Inheritance Ordering:");
        System.out.println("      • Catch blocks MUST be ordered from most specific subclass to most general superclass.");
        System.out.println("      • Multi-catch syntax allows handling multiple unrelated exceptions in a single catch block.");

        // Multi-catch demo
        try {
            triggerUncheckedError("ILLEGAL_ARG");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("      [MULTI-CATCH] Caught via single block (IllegalArgumentException | IllegalStateException): " 
                               + e.getClass().getSimpleName() + " -> " + e.getMessage());
        }

        // Inheritance ordering demo
        try {
            simulateFileIoOperation();
        } catch (FileNotFoundException e) {
            System.out.println("      [SPECIFIC CATCH] Caught FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("      [GENERAL CATCH] Caught broader IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("      [ROOT CATCH] Caught generic Exception: " + e.getMessage());
        }
    }

    private static void triggerUncheckedError(String type) {
        if ("ILLEGAL_ARG".equals(type)) {
            throw new IllegalArgumentException("Invalid parameter value passed to service");
        } else {
            throw new IllegalStateException("System in invalid state for operation");
        }
    }

    private static void simulateFileIoOperation() throws IOException {
        throw new FileNotFoundException("/etc/app/secret.pem (No such file or directory)");
    }

    private static void demonstrateReturnInFinallyAntiPattern() {
        System.out.println("\n   📍 3. CRITICAL GOTCHA: Return Statement inside 'finally' Block:");
        System.out.println("      ⚠️ WARNING: Putting a 'return' statement in a finally block overwrites");
        System.out.println("      the return value from try/catch AND quietly SWALLOWS thrown exceptions!");

        int value = returnWithFinallyTrap();
        System.out.println("      • Caller received returned value: " + value + " (Expected exception was completely swallowed!)");
    }

    @SuppressWarnings("finally")
    private static int returnWithFinallyTrap() {
        try {
            System.out.println("      [TRY] Throwing RuntimeException inside try block...");
            throw new RuntimeException("CRITICAL ERROR IN TRY");
        } catch (RuntimeException e) {
            System.out.println("      [CATCH] Re-throwing RuntimeException...");
            throw e;
        } finally {
            System.out.println("      [FINALLY] Executing return 999 inside finally block!");
            // BAD PRACTICE: Overrides return/exception from try/catch
            return 999;
        }
    }

    private static void demonstrateExceptionSwallowingInFinally() {
        System.out.println("\n   📍 4. Exception Swallowing when 'finally' throws an Exception:");
        System.out.println("      If an exception is thrown in try block AND another in finally block,");
        System.out.println("      the exception in finally SUPPRESSES and DESTROYS the original try exception!");

        try {
            flakyFinallyExecution();
        } catch (Exception e) {
            System.out.println("      • Caller caught exception: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
            System.out.println("      • Was original 'Primary Failure in Try' lost? YES! Only finally exception survived.");
        }
    }

    @SuppressWarnings("finally")
    private static void flakyFinallyExecution() {
        try {
            throw new IllegalStateException("Primary Failure in Try Block");
        } finally {
            // Throwing inside traditional finally hides the original exception above!
            throw new NullPointerException("Secondary Cleanup Failure in Finally Block");
        }
    }

    private static void demonstrateSystemExitBehavior() {
        System.out.println("\n   📍 5. System.exit() Behavior:");
        System.out.println("      • 'finally' block is guaranteed to execute in 99.9% of scenarios.");
        System.out.println("      • EXCEPTIONS: System.exit(status), Runtime.getRuntime().halt(), JVM SIGKILL (kill -9), or OS Power Loss.");
        System.out.println("      • (Note: System.exit is omitted here to prevent stopping the JVM process runner!)");
    }
}
