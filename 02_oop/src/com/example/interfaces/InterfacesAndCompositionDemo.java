package com.example.interfaces;

/**
 * Phase 2.3: Interfaces & Flexible System Design
 * 
 * Main runner demonstrating:
 * 1. Interface fields (public static final), default methods, static methods, private helper methods.
 * 2. Multiple interface implementation & resolving default method collisions with InterfaceName.super.method().
 * 3. Composition Over Inheritance principle (Favoring 'has-a' over 'is-a').
 * 4. Senior SRE Architectural Insights on Contract-First Design & Modular Infrastructure.
 */
public class InterfacesAndCompositionDemo {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 2.3: Interfaces & Flexible System Design");
        System.out.println("========================================================================");

        demonstrateInterfaceFeatures();
        demonstrateMultipleInterfaceImplementation();
        demonstrateCompositionOverInheritance();
        demonstrateSreArchitecturalInsights();

        System.out.println("========================================================================");
        System.out.println("✅ Phase 2.3 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Interface Fields, Default Methods, Static Methods & Private Helper Methods
     */
    private static void demonstrateInterfaceFeatures() {
        System.out.println("\n1️⃣  INTERFACE FEATURES (CONSTANTS, DEFAULT, STATIC & PRIVATE HELPERS):");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   📍 Interface Constants (implicitly 'public static final'):");
        System.out.println("      • ResilientService.DEFAULT_MAX_RETRIES     : " + ResilientService.DEFAULT_MAX_RETRIES);
        System.out.println("      • ResilientService.DEFAULT_RETRY_BACKOFF_MS: " + ResilientService.DEFAULT_RETRY_BACKOFF_MS + " ms");
        System.out.println("      • ResilientService.PROTOCOL_VERSION         : " + ResilientService.PROTOCOL_VERSION);

        System.out.println("\n   📍 Interface Static Methods (Pure utility functions attached to contract):");
        boolean validBackoff = ResilientService.isValidBackoff(500L);
        System.out.println("      • ResilientService.isValidBackoff(500L)? " + validBackoff);

        String telemetry = ResilientService.formatTelemetryEvent("auth-service", "HEALTHY", 120L);
        System.out.println("      • Formatted Telemetry: " + telemetry);

        System.out.println("\n   📍 Invoking Interface Default Method with Internal Private Helper Execution:");
        CloudServiceNode workerNode = new CloudServiceNode("worker-us-east-1a");

        // First attempt: Success path
        System.out.println("   🔹 Scenario A: Successful operation execution via default executeWithRetry():");
        boolean success = workerNode.executeWithRetry("SyncDatabaseIndex");
        System.out.println("      Result: " + (success ? "PASSED" : "FAILED"));

        // Second attempt: Failure with retries
        System.out.println("\n   🔹 Scenario B: Retrying failed operations (simulating network jitter):");
        workerNode.setSimulateFailure(true);
        boolean retryResult = workerNode.executeWithRetry("FlushCache", 2);
        System.out.println("      Result: " + (retryResult ? "PASSED" : "FAILED"));

        System.out.println("\n   📍 Calling default method getServiceHealthSummary():");
        System.out.println("      Report: " + workerNode.getServiceHealthSummary());
    }

    /**
     * 2️⃣ Multiple Interface Implementation & Default Method Collision Disambiguation
     */
    private static void demonstrateMultipleInterfaceImplementation() {
        System.out.println("\n2️⃣  MULTIPLE INTERFACE IMPLEMENTATION & DEFAULT METHOD DISAMBIGUATION:");
        System.out.println("------------------------------------------------------------------------");

        CloudServiceNode multiNode = new CloudServiceNode("gateway-cluster-01");

        System.out.println("   📍 Polymorphic Usage Across Multiple Interface Contracts:");
        
        // Polymorphic reference to AlertNotifier interface
        AlertNotifier notifier = multiNode;
        notifier.sendAlert("CRITICAL", "High CPU Utilization (>90%) on Node");

        // Polymorphic reference to AuditLogger interface
        AuditLogger logger = multiNode;
        logger.logAuditEvent("admin_user", "RESTART_POD", "pod-gateway-01");

        System.out.println("\n   📍 Resolving Default Method Collision (getStatusSummary()):");
        System.out.println("      [Explanation]: Both AlertNotifier and AuditLogger define getStatusSummary().");
        System.out.println("      CloudServiceNode resolves this using AlertNotifier.super.getStatusSummary()");
        System.out.println("      and AuditLogger.super.getStatusSummary() explicitly.\n");

        System.out.println(multiNode.getStatusSummary());
    }

    /**
     * 3️⃣ Composition Over Inheritance Principle (Favoring 'has-a' over 'is-a')
     */
    private static void demonstrateCompositionOverInheritance() {
        System.out.println("\n3️⃣  COMPOSITION OVER INHERITANCE (FAVORING 'HAS-A' OVER 'IS-A'):");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Why Composition?");
        System.out.println("      Inheritance creates brittle tight coupling ('is-a') and leads to Class Explosion:");
        System.out.println("      S3Storage -> EncryptedS3Storage -> EncryptedCompressedS3Storage...");
        System.out.println("      Composition builds flexible systems using pluggable interfaces ('has-a').\n");

        // Building composed system 1: S3 + AES-256 Encryption
        StorageDriver s3Driver = new S3StorageDriver("prod-app-backups");
        EncryptionEngine aesEngine = new AesEncryptionEngine("SuperSecretVaultKey123");

        FlexibleStorageService storageService = new FlexibleStorageService(s3Driver, aesEngine);
        storageService.storeFile("config-backup.json", "{\"database_host\": \"db.internal\"}");
        storageService.retrieveFile("config-backup.json");

        // Dynamic Swap at Runtime: Switch Storage to LocalDisk and Encryption to NoOp
        System.out.println("   📍 Dynamically Swapping Components at Runtime (Zero Class Code Changes!):");
        storageService.setStorageDriver(new LocalStorageDriver("/var/log/app"));
        storageService.setEncryptionEngine(new NoOpEncryptionEngine());

        storageService.storeFile("debug.log", "ERROR: Connection reset by peer");
        storageService.retrieveFile("debug.log");
    }

    /**
     * 4️⃣ Senior SRE Architectural Insights
     */
    private static void demonstrateSreArchitecturalInsights() {
        System.out.println("\n4️⃣  SENIOR SRE ARCHITECTURAL INSIGHTS:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Contract-First API Design with Interfaces:");
        System.out.println("      • Define interfaces (e.g. StorageDriver, ResilientService) before writing concrete logic.");
        System.out.println("      • This allows SREs to mock cloud providers during local testing without paying cloud API costs.");

        System.out.println("\n   💡 Default Methods for Non-Breaking API Evolution:");
        System.out.println("      • Default methods allow adding new capabilities (e.g. executeWithRetry) to shared library");
        System.out.println("        interfaces without breaking existing third-party client codebases.");

        System.out.println("\n   💡 Private Interface Helpers Keep Code Clean:");
        System.out.println("      • Private methods in interfaces prevent code duplication across multiple default methods");
        System.out.println("        without exposing internal helper mechanics in the public API surface.");

        System.out.println("\n   💡 Composition Enables High Testability & Microservice Decoupling:");
        System.out.println("      • By injecting interfaces into domain services, you can easily unit test business logic");
        System.out.println("        by supplying mock or fake implementations.");
    }
}
