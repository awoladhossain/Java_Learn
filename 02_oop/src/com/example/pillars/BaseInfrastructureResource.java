package com.example.pillars;

/**
 * 1️⃣ ABSTRACTION & ENCAPSULATION: Abstract Base Class for Infrastructure Resources.
 * 
 * Demonstrates:
 * - Access Modifiers: private, protected, package-private, public
 * - Domain Invariant validation in constructors and setters
 * - Abstract methods enforcing child implementation contracts
 */
public abstract class BaseInfrastructureResource implements HealthCheckable {

    // Protected Fields: Accessible within package and by subclasses
    protected final String resourceId;
    protected String region;

    // Private Fields: Fully encapsulated; accessible only within this class
    private double costPerHour;
    private boolean deployed;

    // Package-Private (Default) Field: Accessible only within com.example.pillars package
    String internalAuditTag;

    public BaseInfrastructureResource(String resourceId, String region, double costPerHour) {
        // Enforcing Domain Invariants at creation time
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Domain Invariant Violation: Resource ID cannot be null or empty");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Domain Invariant Violation: Region cannot be null or empty");
        }
        if (costPerHour < 0.0) {
            throw new IllegalArgumentException("Domain Invariant Violation: Cost per hour cannot be negative");
        }

        this.resourceId = resourceId;
        this.region = region;
        this.costPerHour = costPerHour;
        this.deployed = false;
        this.internalAuditTag = "AUDIT-" + resourceId.toUpperCase();
    }

    // Abstract Method: Subclasses MUST supply concrete implementation
    public abstract void deploy();

    // Template / Concrete Shared Method
    public void markDeployed() {
        this.deployed = true;
    }

    // Encapsulated Getters & Setters with Domain Invariants
    public String getResourceId() {
        return resourceId;
    }

    public String getRegion() {
        return region;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(double costPerHour) {
        if (costPerHour < 0.0) {
            throw new IllegalArgumentException("Domain Invariant Violation: Cost per hour cannot be negative");
        }
        this.costPerHour = costPerHour;
    }

    public boolean isDeployed() {
        return deployed;
    }

    // Package-private helper method
    String getInternalAuditTag() {
        return internalAuditTag;
    }
}
