package com.example.advanced;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * Section 3.4.1: Enums with Fields, Constructors, and Abstract Methods.
 * 
 * Demonstrates:
 * - Enum internal state (fields, private constructors, getters).
 * - Constant-Specific Class Bodies & Abstract Methods in Enums.
 * - High-Performance Collections: EnumSet (bit-vector optimization) & EnumMap.
 * - SRE use-cases: HTTP Status codes, Deployment Environments, and Severity SLAs.
 */
public class EnumsDeepDiveDemo {

    /**
     * Enum with fields, constructor, lookup methods, and abstract method for constant-specific behavior.
     */
    public enum SeverityLevel {
        CRITICAL("P1 - Critical Outage", 1) {
            @Override
            public double calculateSlaWindowHours() {
                return 0.25; // 15 minute SLA response
            }

            @Override
            public String getEscalationChannel() {
                return "#incident-pager-duty";
            }
        },
        HIGH("P2 - Major Degradation", 2) {
            @Override
            public double calculateSlaWindowHours() {
                return 1.0; // 1 hour SLA response
            }

            @Override
            public String getEscalationChannel() {
                return "#sre-oncall";
            }
        },
        MEDIUM("P3 - Minor Issue", 3) {
            @Override
            public double calculateSlaWindowHours() {
                return 4.0; // 4 hours SLA response
            }

            @Override
            public String getEscalationChannel() {
                return "#dev-support";
            }
        },
        LOW("P4 - Informational", 4) {
            @Override
            public double calculateSlaWindowHours() {
                return 24.0; // 24 hours SLA response
            }

            @Override
            public String getEscalationChannel() {
                return "#backlog-triage";
            }
        };

        private final String description;
        private final int priorityRank;

        SeverityLevel(String description, int priorityRank) {
            this.description = description;
            this.priorityRank = priorityRank;
        }

        public String getDescription() { return description; }
        public int getPriorityRank() { return priorityRank; }

        /**
         * Abstract method implemented by each enum constant.
         */
        public abstract double calculateSlaWindowHours();
        public abstract String getEscalationChannel();

        /**
         * Static utility method for safe reverse lookup by rank.
         */
        public static SeverityLevel fromPriorityRank(int rank) {
            for (SeverityLevel level : values()) {
                if (level.getPriorityRank() == rank) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Unknown severity priority rank: " + rank);
        }
    }

    /**
     * Enum for Deployment Environments.
     */
    public enum Environment {
        DEVELOPMENT(8080, false),
        STAGING(8443, true),
        PRODUCTION(443, true);

        private final int defaultPort;
        private final boolean requireSsl;

        Environment(int defaultPort, boolean requireSsl) {
            this.defaultPort = defaultPort;
            this.requireSsl = requireSsl;
        }

        public int getDefaultPort() { return defaultPort; }
        public boolean isRequireSsl() { return requireSsl; }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.4.1 ENUMS DEEP-DIVE: Fields, Constructors, & Abstract Methods");
        System.out.println("------------------------------------------------------------------------");

        // 1. Enum Constant-Specific Abstract Method Dispatch
        System.out.println("\n--- 1. Enum Abstract Methods & Constant-Specific Behavior ---");
        for (SeverityLevel level : SeverityLevel.values()) {
            System.out.printf("  [%-8s] Rank: %d | SLA: %5.2f hrs | Channel: %-22s | %s\n",
                    level.name(),
                    level.getPriorityRank(),
                    level.calculateSlaWindowHours(),
                    level.getEscalationChannel(),
                    level.getDescription());
        }

        // 2. Reverse Lookup Utility
        System.out.println("\n--- 2. Enum Static Reverse Lookup ---");
        SeverityLevel p1 = SeverityLevel.fromPriorityRank(1);
        System.out.println("Rank 1 Mapped Enum: " + p1 + " (" + p1.getDescription() + ")");

        // 3. EnumSet High Performance Bit-Vector Optimization
        System.out.println("\n--- 3. EnumSet Mechanics (Bitmask-backed Set) ---");
        EnumSet<Environment> secureEnvs = EnumSet.of(Environment.STAGING, Environment.PRODUCTION);
        EnumSet<Environment> allEnvs = EnumSet.allOf(Environment.class);

        System.out.println("Secure Environments (Require SSL): " + secureEnvs);
        System.out.println("All Available Environments       : " + allEnvs);
        System.out.println("Does DEV require SSL?            : " + secureEnvs.contains(Environment.DEVELOPMENT));

        // 4. EnumMap High Performance Direct Array-Indexed Map
        System.out.println("\n--- 4. EnumMap Mechanics (Compact Array-Indexed Map) ---");
        Map<SeverityLevel, Integer> activeIncidents = new EnumMap<>(SeverityLevel.class);
        activeIncidents.put(SeverityLevel.CRITICAL, 0);
        activeIncidents.put(SeverityLevel.HIGH, 2);
        activeIncidents.put(SeverityLevel.MEDIUM, 7);
        activeIncidents.put(SeverityLevel.LOW, 15);

        System.out.println("Active Incidents by Severity:");
        activeIncidents.forEach((sev, count) -> 
            System.out.println("  " + sev + " -> " + count + " active tickets")
        );

        System.out.println("\n💡 SRE Takeaway: EnumMap uses a flat array under the hood indexed by ordinal(),");
        System.out.println("   making it vastly faster and more memory-efficient than HashMap for Enum keys!");
    }
}
