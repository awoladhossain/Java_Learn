package com.example.solid.dip;

/**
 * DIP Compliance:
 * High-Level Service depends on abstraction 'MetricsExporter', NOT on concrete Prometheus or Datadog classes.
 * Swapping telemetry providers requires ZERO code changes inside InfrastructureMonitorService.
 */
public class InfrastructureMonitorService {

    private final MetricsExporter exporter;

    // Dependency Injection via constructor
    public InfrastructureMonitorService(MetricsExporter exporter) {
        this.exporter = exporter;
    }

    public void recordCpuUsage(String node, double cpuPercent) {
        System.out.printf("   ⚙️ [DIP Service] Monitoring node '%s' using exporter (%s):%n",
                node, exporter.getExporterName());
        exporter.exportMetric("node_cpu_utilization_percent", cpuPercent);
    }
}
