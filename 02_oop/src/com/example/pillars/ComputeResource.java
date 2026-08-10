package com.example.pillars;

/**
 * 2️⃣ INHERITANCE, OVERRIDING & OVERLOADING: Compute Instance Resource.
 */
public class ComputeResource extends BaseInfrastructureResource {

    private final String instanceType; // e.g. "c6i.2xlarge"
    private int instanceCount;

    public ComputeResource(String resourceId, String region, double costPerHour, String instanceType, int instanceCount) {
        super(resourceId, region, costPerHour);
        this.instanceType = instanceType;
        this.instanceCount = instanceCount;
    }

    // Abstract Method Implementation (Method Overriding - Runtime Binding)
    @Override
    public void deploy() {
        System.out.printf("   🚀 [ComputeResource] Provisioning %d x %s nodes in region '%s' (ID: %s)...%n",
                instanceCount, instanceType, region, resourceId);
        markDeployed();
    }

    // HealthCheckable Interface Method Implementation
    @Override
    public String getHealthStatus() {
        return isDeployed() 
                ? String.format("Compute Node [%s]: HEALTHY (%d active instances)", resourceId, instanceCount)
                : String.format("Compute Node [%s]: UNHEALTHY (Not Deployed)", resourceId);
    }

    @Override
    public boolean isHealthy() {
        return isDeployed() && instanceCount > 0;
    }

    // 3️⃣ COMPILE-TIME POLYMORPHISM (Method Overloading)
    // Overloaded Method Version 1: Standard scaling
    public void scale(int targetInstances) {
        scale(targetInstances, false);
    }

    // Overloaded Method Version 2: Forced scaling with override flag
    public void scale(int targetInstances, boolean force) {
        if (targetInstances < 0) {
            throw new IllegalArgumentException("Target instance count cannot be negative");
        }
        System.out.printf("   📈 [ComputeResource] Scaling '%s' from %d -> %d instances (Force: %b)...%n",
                resourceId, instanceCount, targetInstances, force);
        this.instanceCount = targetInstances;
    }

    public String getInstanceType() { return instanceType; }
    public int getInstanceCount() { return instanceCount; }
}
