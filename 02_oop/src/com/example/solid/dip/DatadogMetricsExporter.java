package com.example.solid.dip;

public class DatadogMetricsExporter implements MetricsExporter {

    private final String apiKey;

    public DatadogMetricsExporter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void exportMetric(String metricName, double value) {
        System.out.printf("      🐶 [DIP - Datadog] Pushing metric '%s' = %.2f via Agent API [Key: %s...]%n",
                metricName, value, apiKey.substring(0, Math.min(4, apiKey.length())));
    }

    @Override
    public String getExporterName() {
        return "Datadog (Push Model)";
    }
}
