package com.example.solid.srp;

/**
 * SRP (Single Responsibility Principle):
 * This class has ONLY ONE responsibility: parsing and validating deployment configuration.
 * It is isolated from deployment execution and audit logging.
 */
public class DeploymentConfigParser {

    public String parseAndValidate(String rawYamlConfig) {
        if (rawYamlConfig == null || rawYamlConfig.isBlank()) {
            throw new IllegalArgumentException("Config manifest cannot be empty!");
        }
        System.out.println("      📄 [SRP] Parsing deployment manifest lines...");
        return "AppConfig{image='app:v1.2.0', replicas=3, port=8080}";
    }
}
