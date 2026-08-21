package com.example.advanced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Section 3.4.2: Java Records (`record User(...)`).
 * 
 * Demonstrates:
 * - Immutable Data Carriers (automatic field, getter, equals, hashCode, toString generation).
 * - Canonical Constructors vs Compact Constructors (validation & defensive copy).
 * - Custom Instance Methods, Static Factory Methods, and Interface Implementation.
 * - Immutability pitfalls (Defensive copying of mutable collections inside Records).
 */
public class RecordsDeepDiveDemo {

    /**
     * Interface implemented by Records.
     */
    public interface Auditable {
        long timestampMs();
        String createdBy();
    }

    /**
     * Simple Record demonstrating basic syntax and accessor methods.
     */
    public record UserProfile(String id, String username, String email, boolean active) implements Auditable {
        // Record field accessor name matches field: userProfile.id(), userProfile.username()

        @Override
        public long timestampMs() {
            return System.currentTimeMillis();
        }

        @Override
        public String createdBy() {
            return "system_admin";
        }
    }

    /**
     * Record showcasing Compact Constructor (validation & normalization) and Defensive Copying.
     */
    public record ServerCluster(
            String clusterId,
            String region,
            int nodeCount,
            List<String> activeNodes
    ) {
        /**
         * Compact Constructor: Parameter list is omitted.
         * Used for validation, bounds checking, and defensive copies before field assignment.
         */
        public ServerCluster {
            Objects.requireNonNull(clusterId, "clusterId cannot be null");
            Objects.requireNonNull(region, "region cannot be null");

            if (clusterId.isBlank()) {
                throw new IllegalArgumentException("clusterId cannot be blank");
            }
            if (nodeCount <= 0) {
                throw new IllegalArgumentException("nodeCount must be positive: " + nodeCount);
            }

            // Normalization
            clusterId = clusterId.toLowerCase().trim();
            region = region.toUpperCase().trim();

            // Defensive Copy for mutable collection parameter
            // Unmodifiable wrapper ensures Record immutability contract remains unbroken!
            activeNodes = activeNodes != null ? Collections.unmodifiableList(new ArrayList<>(activeNodes)) : List.of();
        }

        /**
         * Custom Instance Method inside Record.
         */
        public String getClusterEndpoint() {
            return String.format("https://%s.%s.internal.net:8443", clusterId, region.toLowerCase());
        }

        /**
         * Static Factory Method inside Record.
         */
        public static ServerCluster createDefaultLocalCluster(String clusterId) {
            return new ServerCluster(clusterId, "us-east-1", 3, List.of("node-1", "node-2", "node-3"));
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.4.2 JAVA RECORDS: Canonical & Compact Constructors, Immutability");
        System.out.println("------------------------------------------------------------------------");

        // 1. Basic Record Features
        System.out.println("\n--- 1. Record Syntactic Sugar & Auto-Generated Contracts ---");
        UserProfile user1 = new UserProfile("usr-101", "alice_sre", "alice@example.com", true);
        UserProfile user2 = new UserProfile("usr-101", "alice_sre", "alice@example.com", true);

        System.out.println("User 1 Record toString() : " + user1);
        System.out.println("Field Accessor user1.username(): " + user1.username());
        System.out.println("Field Accessor user1.email()   : " + user1.email());
        System.out.println("Record Value Equality (.equals): " + user1.equals(user2));
        System.out.println("Record HashCode Equality      : " + (user1.hashCode() == user2.hashCode()));
        System.out.println("Implemented Interface Method   : Created By " + user1.createdBy());

        // 2. Compact Constructor Validation & Normalization
        System.out.println("\n--- 2. Compact Constructor Validation & Normalization ---");
        List<String> initialNodes = new ArrayList<>(List.of("node-a", "node-b"));
        ServerCluster cluster = new ServerCluster("  PROD-K8S-EAST ", " us-east-1 ", 5, initialNodes);

        System.out.println("Normalized Cluster ID : '" + cluster.clusterId() + "'");
        System.out.println("Normalized Region     : '" + cluster.region() + "'");
        System.out.println("Custom Instance Method: " + cluster.getClusterEndpoint());

        // 3. Compact Constructor Defensive Copying Verification
        System.out.println("\n--- 3. Defensive Copying & Immutability Integrity ---");
        System.out.println("Mutating caller's initialNodes list by adding 'node-c'...");
        initialNodes.add("node-c"); // Mutate original caller list

        System.out.println("Caller's mutated list : " + initialNodes);
        System.out.println("Record's activeNodes  : " + cluster.activeNodes() + " (UNMUTATED & IMMUTABLE!)");

        try {
            cluster.activeNodes().add("node-d"); // Attempt direct mutation of record list
        } catch (UnsupportedOperationException e) {
            System.out.println("✅ Expected Protection: Attempting to modify cluster.activeNodes() threw UnsupportedOperationException");
        }

        // 4. Compact Constructor Input Validation Failure
        System.out.println("\n--- 4. Compact Constructor Validation Guard ---");
        try {
            new ServerCluster("dev-cluster", "us-west-1", -2, List.of("node-1"));
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Compact Constructor Guard Caught Invalid State: " + e.getMessage());
        }

        // 5. Static Factory Method
        System.out.println("\n--- 5. Static Factory Method ---");
        ServerCluster defaultCluster = ServerCluster.createDefaultLocalCluster("local-dev");
        System.out.println("Default Cluster Created: " + defaultCluster);

        System.out.println("\n💡 SRE Rule: Always perform DEFENSIVE COPYING of mutable parameters (List, Map, Date)");
        System.out.println("   in Record compact constructors using List.copyOf() or Collections.unmodifiableList().");
    }
}
