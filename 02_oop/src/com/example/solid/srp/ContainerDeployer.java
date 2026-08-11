package com.example.solid.srp;

/**
 * SRP (Single Responsibility Principle):
 * This class has ONLY ONE responsibility: executing container deployments.
 */
public class ContainerDeployer {

    public boolean deployContainer(String parsedConfig) {
        System.out.println("      🚀 [SRP] Deploying container using configuration: " + parsedConfig);
        System.out.println("      ✅ [SRP] Pods created and health probes passing.");
        return true;
    }
}
