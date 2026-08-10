package com.example.pillars;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 2.2: The 4 Pillars of OOP
 * 
 * Main runner demonstrating:
 * 1. Encapsulation & Domain Invariants
 * 2. Inheritance & Class Hierarchies
 * 3. Polymorphism: Dynamic Method Dispatch (Runtime) vs Method Overloading (Compile-Time)
 * 4. Abstraction: Abstract Classes vs Interface Contracts
 * 5. Senior SRE Insights on Polymorphic Orchestration & Liskov Substitution Principle
 */
public class PillarsOfOopDemo {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 2.2: The 4 Pillars of Object-Oriented Programming");
        System.out.println("========================================================================");

        demonstrateEncapsulationAndInvariants();
        demonstrateInheritanceAndAbstraction();
        demonstrateCompileTimePolymorphism();
        demonstrateRuntimePolymorphismAndDispatch();
        demonstrateSrePillarsInsights();

        System.out.println("\n========================================================================");
        System.out.println("✅ Phase 2.2 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Encapsulation & Access Modifiers & Domain Invariants
     */
    private static void demonstrateEncapsulationAndInvariants() {
        System.out.println("\n1️⃣  ENCAPSULATION & DOMAIN INVARIANTS:");
        System.out.println("------------------------------------------------------------------------");

        ComputeResource compute = new ComputeResource("cmp-us-east-1a", "us-east-1", 0.48, "c6i.xlarge", 4);

        System.out.println("   📍 Controlled Field Access (Getters / Setters):");
        System.out.println("      • Resource ID : " + compute.getResourceId());
        System.out.println("      • Initial Cost: $" + compute.getCostPerHour() + "/hr");

        // Enforcing Domain Invariant via Setter
        compute.setCostPerHour(0.52);
        System.out.println("      • Updated Cost: $" + compute.getCostPerHour() + "/hr");

        // Attempting to violate Domain Invariants
        System.out.println("\n   📍 Testing Domain Invariant Validation (Negative Cost Exception):");
        try {
            compute.setCostPerHour(-10.0);
        } catch (IllegalArgumentException ex) {
            System.out.println("      ⚠️ Caught Expected Exception: " + ex.getMessage());
        }

        System.out.println("      • Package-Private Access Test (Internal Audit Tag): " + compute.getInternalAuditTag());
    }

    /**
     * 2️⃣ Inheritance & Abstraction
     */
    private static void demonstrateInheritanceAndAbstraction() {
        System.out.println("\n2️⃣  INHERITANCE & ABSTRACTION:");
        System.out.println("------------------------------------------------------------------------");

        // Abstract reference type pointing to concrete subclass instance
        BaseInfrastructureResource dbResource = new DatabaseResource(
                "db-primary-pg", "us-west-2", 1.85, "PostgreSQL 16.2", 500, true
        );

        System.out.println("   📍 Interacting via Abstract Class Contract (BaseInfrastructureResource):");
        System.out.println("      • Deployed before call? " + dbResource.isDeployed());
        System.out.print("      ");
        dbResource.deploy();
        System.out.println("      • Deployed after call?  " + dbResource.isDeployed());
    }

    /**
     * 3️⃣ Compile-Time Polymorphism (Method Overloading)
     */
    private static void demonstrateCompileTimePolymorphism() {
        System.out.println("\n3️⃣  COMPILE-TIME POLYMORPHISM (METHOD OVERLOADING):");
        System.out.println("------------------------------------------------------------------------");

        ComputeResource compute = new ComputeResource("cmp-app-cluster", "us-east-1", 0.96, "m6i.2xlarge", 8);

        System.out.println("   📍 Invoking Overloaded Method Version 1 (scale(int targetInstances)):");
        compute.scale(12);

        System.out.println("\n   📍 Invoking Overloaded Method Version 2 (scale(int targetInstances, boolean force)):");
        compute.scale(20, true);
    }

    /**
     * 4️⃣ Runtime Polymorphism & Dynamic Method Dispatch
     */
    private static void demonstrateRuntimePolymorphismAndDispatch() {
        System.out.println("\n4️⃣  RUNTIME POLYMORPHISM (DYNAMIC METHOD DISPATCH):");
        System.out.println("------------------------------------------------------------------------");

        // Polymorphic Collection of Abstract Base Class references
        List<BaseInfrastructureResource> infraCluster = new ArrayList<>();
        infraCluster.add(new ComputeResource("k8s-worker-grp1", "us-east-1", 1.20, "c6i.4xlarge", 16));
        infraCluster.add(new DatabaseResource("rds-postgres-read1", "us-east-1", 0.95, "PostgreSQL 16", 250, false));
        infraCluster.add(new ComputeResource("k8s-worker-grp2", "us-east-1", 0.60, "c6i.2xlarge", 8));

        System.out.println("   📍 Executing Polymorphic Deployment Engine:");
        for (BaseInfrastructureResource resource : infraCluster) {
            // Dynamic Method Dispatch: JVM resolves resource.deploy() at runtime to actual Heap object type
            resource.deploy();
        }

        System.out.println("\n   📍 Executing Polymorphic Health Check Engine (via HealthCheckable interface):");
        for (HealthCheckable healthItem : infraCluster) {
            // Interface dynamic dispatch
            System.out.println("      • " + healthItem.getHealthStatus());
        }
    }

    /**
     * 5️⃣ Senior SRE Insights on 4 Pillars
     */
    private static void demonstrateSrePillarsInsights() {
        System.out.println("\n5️⃣  SENIOR SRE ARCHITECTURE INSIGHTS:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Liskov Substitution Principle (LSP) in Polymorphic Infra Engines:");
        System.out.println("      • Subclasses (ComputeResource, DatabaseResource) MUST be substitutable for their superclass");
        System.out.println("        (BaseInfrastructureResource) without breaking application behavior or violating expectations.");

        System.out.println("\n   💡 Prefer Interfaces for Contract Abstraction:");
        System.out.println("      • Interfaces (e.g. HealthCheckable) allow decoupling cloud infrastructure orchestration logic");
        System.out.println("        from specific class hierarchies, enabling seamless mock testing and provider swapping.");

        System.out.println("\n   💡 Defensive Encapsulation:");
        System.out.println("      • Never expose raw mutable internal fields without validation.");
        System.out.println("      • Encapsulating domain invariants in setters prevents corrupt system state across microservices.");
    }
}
