package com.example.solid.srp;

/**
 * SRP (Single Responsibility Principle):
 * This class has ONLY ONE responsibility: recording immutable audit records for deployments.
 */
public class DeploymentAuditLogger {

    public void recordAudit(String user, String configSummary, boolean status) {
        System.out.printf("      📝 [SRP] AUDIT LOG: User='%s', Status='%s', Config='%s'%n",
                user, status ? "SUCCESS" : "FAILED", configSummary);
    }
}
