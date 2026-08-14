package com.example.exceptions;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Phase 3.1 Demonstration: Exception Chaining & Suppressed Exceptions
 * 
 * Teaches:
 * 1. Exception Chaining pattern: Wrapping technical exceptions into domain exceptions while preserving root cause.
 * 2. Unwrapping exception chains via e.getCause().
 * 3. Manual Suppressed Exceptions using Throwable.addSuppressed().
 * 4. Production observability & preserving full stack trace diagnostics for SRE troubleshooting.
 */
public class ExceptionChainingAndSuppressionDemo {

    public static void runDemo() {
        System.out.println("\n========================================================================");
        System.out.println("4️⃣  EXCEPTION CHAINING & MANUAL SUPPRESSED EXCEPTIONS");
        System.out.println("========================================================================");

        demonstrateExceptionChaining();
        demonstrateRootCauseUnwrapping();
        demonstrateManualExceptionSuppression();
        printSreFaultToleranceRules();
    }

    /**
     * 1. Exception Chaining: Wrapping technical low-level exceptions in business domain exceptions.
     */
    private static void demonstrateExceptionChaining() {
        System.out.println("\n   📍 1. Exception Chaining Pattern (Root Cause Preservation):");
        System.out.println("      Converting low-level SQLException into high-level DatabaseConnectionException.");

        try {
            fetchCustomerData("cust_1001");
        } catch (DatabaseConnectionException e) {
            System.out.println("      [DOMAIN HANDLER] High-Level Exception Caught:");
            System.out.println("      • Domain Exception: " + e.getClass().getName());
            System.out.println("      • Message: " + e.getMessage());
            System.out.println("      • Root Cause (e.getCause()): " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
        }
    }

    private static void fetchCustomerData(String customerId) throws DatabaseConnectionException {
        try {
            // Low-level database call failure
            simulateDatabaseDriverCall();
        } catch (SQLException sqlEx) {
            // Wrapping SQLException into DatabaseConnectionException while passing sqlEx as root cause
            throw new DatabaseConnectionException(
                    "jdbc:postgresql://db-primary.internal:5432/orders",
                    "Failed to query customer database records for ID: " + customerId,
                    sqlEx
            );
        }
    }

    private static void simulateDatabaseDriverCall() throws SQLException {
        throw new SQLException("Connection timed out after 3000ms (SQLState: 08001, ErrorCode: 1017)", "08001", 1017);
    }

    /**
     * 2. Traversing deep exception chains to locate root cause.
     */
    private static void demonstrateRootCauseUnwrapping() {
        System.out.println("\n   📍 2. Traversing Deep Exception Chains:");

        try {
            performMultiLayerOperation();
        } catch (Throwable t) {
            System.out.println("      [TOP LEVEL EXCEPTION] " + t.getClass().getSimpleName() + ": " + t.getMessage());
            
            Throwable cause = t.getCause();
            int depth = 1;
            while (cause != null) {
                System.out.println("      └─> [CAUSED BY Depth " + depth + "] " + cause.getClass().getName() + ": " + cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
        }
    }

    private static void performMultiLayerOperation() {
        try {
            layer2Service();
        } catch (Exception e) {
            throw new ResourceNotFoundException("CustomerRecord", "cust_1001", e);
        }
    }

    private static void layer2Service() throws Exception {
        try {
            layer1Repository();
        } catch (Exception e) {
            throw new Exception("Layer 2 Service processing failure", e);
        }
    }

    private static void layer1Repository() throws IOException {
        throw new IOException("Socket error reading payload from disk storage array");
    }

    /**
     * 3. Manual Exception Suppression using addSuppressed().
     */
    private static void demonstrateManualExceptionSuppression() {
        System.out.println("\n   📍 3. Manual Exception Suppression (Throwable.addSuppressed):");
        System.out.println("      When performing multi-step cleanup, secondary errors can be manually added");
        System.out.println("      to the primary exception without losing context.");

        BaseDomainException mainException = new ResourceNotFoundException("Order", "ord_7711");

        // Simulating background cleanup errors during exception propagation
        IOException cleanupError1 = new IOException("Failed to flush log buffer file");
        SQLException cleanupError2 = new SQLException("Failed to rollback active transaction state");

        mainException.addSuppressed(cleanupError1);
        mainException.addSuppressed(cleanupError2);

        System.out.println("      • Primary Exception Message: " + mainException.getMessage());
        System.out.println("      • Suppressed Count: " + mainException.getSuppressed().length);
        for (Throwable supp : mainException.getSuppressed()) {
            System.out.println("        -> Suppressed: " + supp.getClass().getSimpleName() + " - " + supp.getMessage());
        }
    }

    private static void printSreFaultToleranceRules() {
        System.out.println("\n   💡 SENIOR SRE FAULT TOLERANCE GOLDEN RULES:");
        System.out.println("      1. Always preserve the original Throwable cause when re-throwing exceptions!");
        System.out.println("         Writing 'throw new MyException(e.getMessage())' destroys the stack trace!");
        System.out.println("      2. Log exceptions EXACTLY ONCE at the top-level boundary (e.g. Controller / API Handler).");
        System.out.println("         Logging and re-throwing creates log spam and duplicate stack traces in Grafana/Loki.");
        System.out.println("      3. Include Correlation IDs in exception messages for end-to-end distributed tracing across microservices.");
    }
}
