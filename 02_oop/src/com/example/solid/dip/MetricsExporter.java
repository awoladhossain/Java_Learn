package com.example.solid.dip;

public interface MetricsExporter {
    void exportMetric(String metricName, double value);
    String getExporterName();
}
