package com.example.pillars;

/**
 * Concrete Database Resource demonstrating Inheritance and Runtime Polymorphism.
 */
public class DatabaseResource extends BaseInfrastructureResource {

    private final String dbEngine; // e.g. "PostgreSQL 16.2"
    private final int storageGb;
    private final boolean multiAz;

    public DatabaseResource(String resourceId, String region, double costPerHour, String dbEngine, int storageGb, boolean multiAz) {
        super(resourceId, region, costPerHour);
        this.dbEngine = dbEngine;
        this.storageGb = storageGb;
        this.multiAz = multiAz;
    }

    @Override
    public void deploy() {
        System.out.printf("   💾 [DatabaseResource] Provisioning %s (%d GB, Multi-AZ: %b) in region '%s' (ID: %s)...%n",
                dbEngine, storageGb, multiAz, region, resourceId);
        markDeployed();
    }

    @Override
    public String getHealthStatus() {
        return isDeployed()
                ? String.format("Database Node [%s]: ONLINE (%s, Multi-AZ: %b)", resourceId, dbEngine, multiAz)
                : String.format("Database Node [%s]: OFFLINE (Not Deployed)", resourceId);
    }

    @Override
    public boolean isHealthy() {
        return isDeployed();
    }

    public String getDbEngine() { return dbEngine; }
    public int getStorageGb() { return storageGb; }
    public boolean isMultiAz() { return multiAz; }
}
