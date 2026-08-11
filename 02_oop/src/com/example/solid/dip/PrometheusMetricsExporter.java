package com.example.solid.dip;

public class PrometheusMetricsExporter implements MetricsExporter {

    @Override
    public void exportMetric(String metricName, double value) {
        System.out.printf("      📊 [DIP - Prometheus] Exporting counter '%s' = %.2f to /metrics endpoint%n",
                metricName, value);
    }

    @Override
    public String getExporterName() {
        return "Prometheus (Pull Model)";
    }
}
