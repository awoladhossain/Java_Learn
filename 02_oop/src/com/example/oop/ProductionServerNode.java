package com.example.oop;

/**
 * Child Class illustrating:
 * 1. Inheritance via 'extends'
 * 2. 'super' keyword usage (calling parent constructor super(...) and super.getNodeSummary())
 * 3. 'final' class modifier (prevents subclassing / inheritance)
 */
public final class ProductionServerNode extends ServerNode {

    private final String environment; // e.g. "prod-us-east-1"
    private final String deploymentZone;

    public ProductionServerNode(String nodeId, String hostname, int port, String environment, String deploymentZone) {
        // Must call parent parameterized constructor using 'super(...)' as first statement
        super(nodeId, hostname, port);
        this.environment = environment;
        this.deploymentZone = deploymentZone;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getDeploymentZone() {
        return deploymentZone;
    }

    @Override
    public String getNodeSummary() {
        // Using 'super' to invoke parent implementation and augment with child state
        return super.getNodeSummary() + String.format(" [Env: %s | Zone: %s]", environment, deploymentZone);
    }
}
