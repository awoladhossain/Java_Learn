package com.example.solid;

import com.example.solid.dip.DatadogMetricsExporter;
import com.example.solid.dip.InfrastructureMonitorService;
import com.example.solid.dip.MetricsExporter;
import com.example.solid.dip.PrometheusMetricsExporter;
import com.example.solid.isp.CacheManageable;
import com.example.solid.isp.RedisCacheWorker;
import com.example.solid.lsp.PrimaryPostgresDb;
import com.example.solid.lsp.ReadReplicaPostgresDb;
import com.example.solid.lsp.ReadableDatabase;
import com.example.solid.lsp.WritableDatabase;
import com.example.solid.ocp.NotificationChannel;
import com.example.solid.ocp.NotificationService;
import com.example.solid.ocp.PagerDutyChannel;
import com.example.solid.ocp.SlackChannel;
import com.example.solid.srp.ContainerDeployer;
import com.example.solid.srp.DeploymentAuditLogger;
import com.example.solid.srp.DeploymentConfigParser;

import java.util.List;

/**
 * Phase 2.4: Software Design Principles (SOLID & Beyond)
 * 
 * Main runner demonstrating:
 * 1. Single Responsibility Principle (SRP)
 * 2. Open/Closed Principle (OCP)
 * 3. Liskov Substitution Principle (LSP)
 * 4. Interface Segregation Principle (ISP)
 * 5. Dependency Inversion Principle (DIP)
 * 6. Software Pragmatism: DRY, KISS, and YAGNI.
 */
public class SolidAndBeyondDemo {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 2.4: Software Design Principles (SOLID & Beyond)");
        System.out.println("========================================================================");

        demonstrateSrp();
        demonstrateOcp();
        demonstrateLsp();
        demonstrateIsp();
        demonstrateDip();
        demonstrateDryKissYagni();

        System.out.println("========================================================================");
        System.out.println("✅ Phase 2.4 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Single Responsibility Principle (SRP)
     * A class should have one, and only one, reason to change.
     */
    private static void demonstrateSrp() {
        System.out.println("\n1️⃣  SINGLE RESPONSIBILITY PRINCIPLE (SRP):");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   💡 Concept: Separate parsing, execution, and audit logging into single-purpose classes.\n");

        DeploymentConfigParser parser = new DeploymentConfigParser();
        ContainerDeployer deployer = new ContainerDeployer();
        DeploymentAuditLogger auditLogger = new DeploymentAuditLogger();

        String rawYaml = "image: nginx:latest\nreplicas: 3";
        String parsedConfig = parser.parseAndValidate(rawYaml);
        boolean success = deployer.deployContainer(parsedConfig);
        auditLogger.recordAudit("devops_user", parsedConfig, success);
    }

    /**
     * 2️⃣ Open/Closed Principle (OCP)
     * Software entities should be open for extension, but closed for modification.
     */
    private static void demonstrateOcp() {
        System.out.println("\n2️⃣  OPEN/CLOSED PRINCIPLE (OCP):");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   💡 Concept: Add new notification channels without altering NotificationService code.\n");

        List<NotificationChannel> channels = List.of(
                new SlackChannel("https://hooks.slack.com/services/sre-alerts"),
                new PagerDutyChannel("pd-service-key-7781")
        );

        NotificationService notificationService = new NotificationService(channels);
        notificationService.dispatchAlert("High Memory Usage detected on prod-db-01 (>88%)");
    }

    /**
     * 3️⃣ Liskov Substitution Principle (LSP)
     * Subtypes must be substitutable for their base types without breaking client expectations.
     */
    private static void demonstrateLsp() {
        System.out.println("\n3️⃣  LISKOV SUBSTITUTION PRINCIPLE (LSP):");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   💡 Concept: ReadReplicaPostgresDb only implements ReadableDatabase interface,");
        System.out.println("      preventing runtime UnsupportedOperationException when writing.\n");

        ReadableDatabase primaryDb = new PrimaryPostgresDb("jdbc:postgresql://primary.internal:5432/proddb");
        ReadableDatabase replicaDb = new ReadReplicaPostgresDb("jdbc:postgresql://replica1.internal:5432/proddb");

        // Polymorphic query function accepting any ReadableDatabase without risk of runtime exceptions
        executeQueryOnDb(primaryDb, "SELECT count(*) FROM users");
        executeQueryOnDb(replicaDb, "SELECT count(*) FROM audit_events");

        // WritableDatabase contract explicitly requires write capabilities
        WritableDatabase writablePrimary = new PrimaryPostgresDb("jdbc:postgresql://primary.internal:5432/proddb");
        writablePrimary.executeWrite("UPDATE accounts SET balance = balance + 100 WHERE id = 42");
    }

    private static void executeQueryOnDb(ReadableDatabase db, String sql) {
        String result = db.executeQuery(sql);
        System.out.printf("      ✅ Query executed on [%s] -> %s%n", db.getDatabaseUri(), result);
    }

    /**
     * 4️⃣ Interface Segregation Principle (ISP)
     * Clients should not be forced to depend upon interfaces that they do not use.
     */
    private static void demonstrateIsp() {
        System.out.println("\n4️⃣  INTERFACE SEGREGATION PRINCIPLE (ISP):");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   💡 Concept: Fine-grained interfaces (CacheManageable vs Deployable).");
        System.out.println("      RedisCacheWorker is not burdened with deployment or migration methods.\n");

        CacheManageable cacheWorker = new RedisCacheWorker("redis-cluster.prod.internal:6379");
        cacheWorker.invalidateKey("user:session:99481");
        cacheWorker.flushAll();
    }

    /**
     * 5️⃣ Dependency Inversion Principle (DIP)
     * High-level modules should depend on abstractions, not on concrete implementations.
     */
    private static void demonstrateDip() {
        System.out.println("\n5️⃣  DEPENDENCY INVERSION PRINCIPLE (DIP):");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   💡 Concept: High-level InfrastructureMonitorService depends on MetricsExporter interface.\n");

        // Swapping exporter implementations at runtime via constructor dependency injection
        MetricsExporter prometheus = new PrometheusMetricsExporter();
        InfrastructureMonitorService monitor1 = new InfrastructureMonitorService(prometheus);
        monitor1.recordCpuUsage("k8s-node-us-east-1a", 76.4);

        System.out.println();
        MetricsExporter datadog = new DatadogMetricsExporter("dd_api_key_xyz9876");
        InfrastructureMonitorService monitor2 = new InfrastructureMonitorService(datadog);
        monitor2.recordCpuUsage("k8s-node-us-east-1b", 82.1);
    }

    /**
     * 6️⃣ Beyond SOLID: DRY, KISS, and YAGNI
     */
    private static void demonstrateDryKissYagni() {
        System.out.println("\n6️⃣  SOFTWARE PRAGMATISM (DRY, KISS, YAGNI):");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 DRY (Don't Repeat Yourself):");
        System.out.println("      • Avoid copy-pasting business logic across controllers or handlers.");
        System.out.println("      • Encapsulate shared validation or formatting in reusable components/interfaces.");

        System.out.println("\n   💡 KISS (Keep It Simple, Stupid):");
        System.out.println("      • Avoid over-designing complex abstract frameworks when a simple method or class works.");
        System.out.println("      • Readability and simplicity reduce mean-time-to-resolution (MTTR) during outages.");

        System.out.println("\n   💡 YAGNI (You Aren't Gonna Need It):");
        System.out.println("      • Do NOT build speculative features (e.g. supporting 5 unneeded databases) 'just in case'.");
        System.out.println("      • Write code for current requirements; clean SOLID design allows adding future needs easily.");
    }
}
