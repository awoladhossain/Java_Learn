package com.example.modern;

/**
 * Section 5.3.3: Pattern Matching & Switch Expressions (Java 14 - 21).
 * 
 * Demonstrates:
 * - Pattern Matching for instanceof (Java 16 smart casting & variable scoping).
 * - Switch Expressions (Java 14 arrow syntax, yield, exhaustiveness).
 * - Pattern Matching for switch (Java 21 type patterns, null handling, guarded when clauses).
 * - Record Patterns (Java 21 deconstruction of records in patterns).
 * - Sealed Hierarchy Exhaustive Pattern Matching without default branch.
 */
public class PatternMatchingDemo {

    // Domain Sealed Interface Hierarchy for Telemetry Events
    public sealed interface TelemetryEvent permits CpuAlert, MemoryAlert, NetworkAlert {}

    public record CpuAlert(String serverId, double cpuPercentage, boolean isSpike) implements TelemetryEvent {}
    public record MemoryAlert(String serverId, double memoryUsedMb, double memoryTotalMb) implements TelemetryEvent {}
    public record NetworkAlert(String serverId, String targetIp, long droppedPackets) implements TelemetryEvent {}

    // Record with nested record for Record Pattern Deconstruction demo
    public record Location(String datacenter, String rackId) {}
    public record NodeInfo(String nodeName, Location location) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.3.3 PATTERN MATCHING & SWITCH EXPRESSIONS (Java 14-21)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Pattern Matching for instanceof (Java 16)
        // ==========================================
        System.out.println("\n--- 1. Pattern Matching for instanceof ---");

        Object payload1 = "  CRITICAL_DATABASE_DEADLOCK  ";
        Object payload2 = 99.42;

        processPayloadLegacy(payload1);
        processPayloadModern(payload1);
        processPayloadModern(payload2);

        // Pattern matching in short-circuiting boolean expressions
        if (payload1 instanceof String s && s.trim().startsWith("CRITICAL")) {
            System.out.println("Pattern matching with guard in if-condition: Detected Critical Event -> " + s.trim());
        }

        // ==========================================
        // 2. Switch Expressions (Java 14)
        // ==========================================
        System.out.println("\n--- 2. Switch Expressions (Arrow Syntax & Yield) ---");

        String environment = "STAGING";

        // Switch expression returning a value using arrow syntax
        int maxRetries = switch (environment) {
            case "DEV", "TEST" -> 1;
            case "STAGING" -> 3;
            case "PRODUCTION" -> {
                System.out.println("   [CONFIG] Calculating high-availability retries for PRODUCTION...");
                yield 5; // yield keyword returns value from block
            }
            default -> throw new IllegalArgumentException("Unknown environment: " + environment);
        };

        System.out.println("Max Retries for environment [" + environment + "]: " + maxRetries);

        // ==========================================
        // 3. Pattern Matching for switch with Guarded 'when' Clauses (Java 21)
        // ==========================================
        System.out.println("\n--- 3. Pattern Matching for switch with Guarded 'when' Clauses ---");

        TelemetryEvent event1 = new CpuAlert("k8s-node-01", 95.5, true);
        TelemetryEvent event2 = new MemoryAlert("k8s-node-02", 7800.0, 8000.0);
        TelemetryEvent event3 = new NetworkAlert("k8s-node-03", "10.0.0.99", 50);

        analyzeTelemetryEvent(event1);
        analyzeTelemetryEvent(event2);
        analyzeTelemetryEvent(event3);
        analyzeTelemetryEvent(null); // Demonstrating null handling in switch!

        // ==========================================
        // 4. Record Patterns & Deconstruction (Java 21)
        // ==========================================
        System.out.println("\n--- 4. Record Deconstruction Patterns ---");

        NodeInfo node = new NodeInfo("prod-worker-99", new Location("us-east-dc1", "rack-42"));

        // Deconstructing nested Record pattern directly inside switch case signature!
        String locationReport = switch (node) {
            case NodeInfo(String name, Location(String dc, String rack)) -> 
                String.format("Node [%s] located in Datacenter [%s], Rack [%s]", name, dc, rack);
        };

        System.out.println("Record Deconstruction Result: " + locationReport);

        System.out.println("\n💡 SRE Insight: Pattern matching for switch combined with Sealed Interfaces enforces exhaustive compile-time checking.");
        System.out.println("   Adding a new event type to the interface forces developers to update all handling switch expressions across the codebase!");
    }

    private static void processPayloadLegacy(Object payload) {
        // Legacy verbose casting approach
        if (payload instanceof String) {
            String s = (String) payload; // Explicit cast required!
            System.out.println("   [LEGACY CAST] String length: " + s.trim().length());
        }
    }

    private static void processPayloadModern(Object payload) {
        // Modern Pattern Matching for instanceof
        if (payload instanceof String s) { // Binding variable 's' automatically cast
            System.out.println("   [MODERN SMART-CAST] String trimmed: " + s.trim());
        } else if (payload instanceof Double d) {
            System.out.println("   [MODERN SMART-CAST] Double rounded: " + Math.round(d));
        }
    }

    private static void analyzeTelemetryEvent(TelemetryEvent event) {
        // Pattern Matching in Switch with 'null' handling and guarded 'when' clauses
        String report = switch (event) {
            case null -> "   [WARN] Received null telemetry event object.";
            
            case CpuAlert cpu when cpu.cpuPercentage() > 90.0 && cpu.isSpike() -> 
                String.format("   [CRITICAL CPU SPIKE] Server %s at %.1f%%", cpu.serverId(), cpu.cpuPercentage());
            
            case CpuAlert cpu -> 
                String.format("   [INFO CPU] Server %s at %.1f%%", cpu.serverId(), cpu.cpuPercentage());
            
            case MemoryAlert mem when (mem.memoryUsedMb() / mem.memoryTotalMb()) > 0.90 -> 
                String.format("   [CRITICAL OOM RISK] Server %s memory near capacity (%.0f/%.0f MB)", 
                    mem.serverId(), mem.memoryUsedMb(), mem.memoryTotalMb());
            
            case MemoryAlert mem -> 
                String.format("   [INFO MEM] Server %s memory (%.0f/%.0f MB)", 
                    mem.serverId(), mem.memoryUsedMb(), mem.memoryTotalMb());
            
            case NetworkAlert net -> 
                String.format("   [NET ALERT] Server %s dropped %d packets to %s", 
                    net.serverId(), net.droppedPackets(), net.targetIp());
        };

        System.out.println(report);
    }
}
