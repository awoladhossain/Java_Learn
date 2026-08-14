package com.example.exceptions;

/**
 * Phase 3.1 Demonstration: try-with-resources & AutoCloseable Resource Management
 * 
 * Teaches:
 * 1. Legacy manual cleanup pitfalls (memory leaks & file descriptor leaks).
 * 2. Modern try-with-resources construct (Java 7+ and Java 9+ effectively final syntax).
 * 3. LIFO (Last-In, First-Out) closing semantics for multiple resources.
 * 4. Automatic suppressed exception tracking when close() throws an exception.
 */
public class TryWithResourcesDemo {

    public static void runDemo() {
        System.out.println("\n========================================================================");
        System.out.println("3️⃣  TRY-WITH-RESOURCES & AUTOCLOSEABLE (FAULT-TOLERANT CLEANUP)");
        System.out.println("========================================================================");

        demonstrateLegacyManualCleanupPitfall();
        demonstrateTryWithResourcesSingleResource();
        demonstrateTryWithResourcesMultipleLIFO();
        demonstrateJava9EffectivelyFinalSyntax();
        demonstrateSuppressedExceptionTracking();
    }

    /**
     * 1. Demonstrates old Java 6 manual cleanup inside finally block.
     * Shows how verbose, error-prone, and prone to resource leaks it was!
     */
    private static void demonstrateLegacyManualCleanupPitfall() {
        System.out.println("\n   📍 1. Legacy Manual Resource Cleanup (Java 6 Pattern):");
        System.out.println("      Requires nested try-catch in finally block to avoid masking primary exceptions!");

        ManagedDatabaseConnection dbConn = null;
        try {
            dbConn = new ManagedDatabaseConnection("legacy-db-pool-01");
            dbConn.executeQuery("SELECT * FROM users");
        } catch (Exception e) {
            System.out.println("      Caught error: " + e.getMessage());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Exception closeEx) {
                    System.out.println("      Error during manual close in finally: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * 2. Demonstrates modern try-with-resources with single AutoCloseable resource.
     */
    private static void demonstrateTryWithResourcesSingleResource() {
        System.out.println("\n   📍 2. Modern try-with-resources (Single Resource):");
        System.out.println("      Resource automatically closed upon block exit (normal OR exceptional).");

        try (ManagedDatabaseConnection db = new ManagedDatabaseConnection("db-connection-primary")) {
            db.executeQuery("UPDATE accounts SET balance = balance - 100 WHERE id = 42");
        } catch (Exception e) {
            System.out.println("      Caught exception during execution: " + e.getMessage());
        }
        System.out.println("      • Notice: close() was invoked automatically BEFORE entering outer code scope!");
    }

    /**
     * 3. Demonstrates multiple resources in try-with-resources.
     * Closing order is strict LIFO (Last declared, first closed).
     */
    private static void demonstrateTryWithResourcesMultipleLIFO() {
        System.out.println("\n   📍 3. Multiple Resources & LIFO Closing Semantics:");
        System.out.println("      Resources are initialized left-to-right, but CLOSED right-to-left (LIFO order).");

        try (ManagedDatabaseConnection db = new ManagedDatabaseConnection("db-prod-write");
             ManagedFileDescriptor file = new ManagedFileDescriptor("/var/log/transactions.log")) {

            db.executeQuery("INSERT INTO transactions VALUES (101, 'USD', 500.00)");
            file.writeData("Transaction 101 committed successfully");

        } catch (Exception e) {
            System.out.println("      Caught error: " + e.getMessage());
        }
    }

    /**
     * 4. Demonstrates Java 9+ syntax allowing pre-allocated effectively final variables in try-with-resources.
     */
    private static void demonstrateJava9EffectivelyFinalSyntax() {
        System.out.println("\n   📍 4. Java 9+ Effectively Final Resource Syntax:");
        System.out.println("      Can pass existing effectively final AutoCloseable references directly into try().");

        ManagedFileDescriptor auditLog = new ManagedFileDescriptor("/var/audit/security.log");

        // Java 9+ Syntax: try (auditLog) instead of try (ManagedFileDescriptor log = auditLog)
        try (auditLog) {
            auditLog.writeData("ADMIN_USER session created");
        } catch (Exception e) {
            System.out.println("      Error: " + e.getMessage());
        }
    }

    /**
     * 5. Demonstrates how try-with-resources handles exceptions thrown during close().
     * Primary exception is preserved; close() exception is attached as Suppressed Exception!
     */
    private static void demonstrateSuppressedExceptionTracking() {
        System.out.println("\n   📍 5. Automatic Suppressed Exception Preservation:");
        System.out.println("      When both try block AND close() throw exceptions:");
        System.out.println("      - Primary exception from try block is thrown to caller.");
        System.out.println("      - Secondary exception from close() is attached as Suppressed Exception via e.getSuppressed().");

        try (ManagedDatabaseConnection db = new ManagedDatabaseConnection("failing-close-conn")) {
            db.setFailOnClose(true); // Force close() to throw IllegalStateException
            
            System.out.println("      [TRY BLOCK] Simulating primary operational query crash...");
            throw new ResourceNotFoundException("UserAccount", "usr_998822");

        } catch (Exception e) {
            System.out.println("\n      [PRIMARY EXCEPTION CAUGHT IN CALLER]:");
            System.out.println("      • Type: " + e.getClass().getName());
            System.out.println("      • Message: " + e.getMessage());

            Throwable[] suppressed = e.getSuppressed();
            System.out.println("\n      [SUPPRESSED EXCEPTIONS ATTACHED (" + suppressed.length + ")]: ");
            for (Throwable s : suppressed) {
                System.out.println("      -> Suppressed Exception: " + s.getClass().getName() + ": " + s.getMessage());
            }
        }
    }
}
