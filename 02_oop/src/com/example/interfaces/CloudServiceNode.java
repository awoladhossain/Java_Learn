package com.example.interfaces;

/**
 * Concrete implementation class demonstrating:
 * 1. Multiple Interface Implementation in Java.
 * 2. Explicit Default Method Disambiguation/Resolution via InterfaceName.super.method().
 */
public class CloudServiceNode implements ResilientService, AlertNotifier, AuditLogger {

    private final String nodeName;
    private boolean simulateFailure = false;

    public CloudServiceNode(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    // --- ResilientService Implementation ---
    @Override
    public String getServiceName() {
        return nodeName;
    }

    @Override
    public boolean executeOperation(String operationName) {
        if (simulateFailure) {
            System.out.printf("      ❌ [%s] Operation '%s' failed due to network jitter.%n", nodeName, operationName);
            throw new RuntimeException("Transient connection error");
        }
        System.out.printf("      ⚡ [%s] Operation '%s' executed successfully.%n", nodeName, operationName);
        return true;
    }

    // --- AlertNotifier Implementation ---
    @Override
    public void sendAlert(String severity, String message) {
        System.out.printf("      🚨 ALERT [%s] Node: %s - %s%n", severity, nodeName, message);
    }

    // --- AuditLogger Implementation ---
    @Override
    public void logAuditEvent(String user, String action, String target) {
        System.out.printf("      📝 AUDIT [%s] User '%s' performed '%s' on '%s'%n", nodeName, user, action, target);
    }

    // --- Resolving Default Method Collision ---
    // Both AlertNotifier and AuditLogger define default String getStatusSummary()
    // The implementing class MUST override getStatusSummary() to resolve compiler error.
    @Override
    public String getStatusSummary() {
        // Disambiguate using InterfaceName.super.methodName()
        String alertStatus = AlertNotifier.super.getStatusSummary();
        String auditStatus = AuditLogger.super.getStatusSummary();
        return String.format("CloudServiceNode[%s] Combined Status:\n        -> %s\n        -> %s",
                nodeName, alertStatus, auditStatus);
    }
}
