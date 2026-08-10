package com.example.pillars;

/**
 * Abstraction Contract: HealthCheckable interface
 */
public interface HealthCheckable {
    String getHealthStatus();
    boolean isHealthy();
}
