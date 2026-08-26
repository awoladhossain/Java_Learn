package com.example.patterns.behavioral;

import java.util.ArrayList;
import java.util.List;

/**
 * 🛠️ Behavioral Pattern: Observer
 * 
 * Defines a one-to-many dependency between objects so that when one object changes state,
 * all its dependents are notified and updated automatically.
 * E.g., SRE Telemetry & Alerting event publisher broadcasting metrics to multiple monitoring services.
 */
public class ObserverPattern {

    // Metric Event Data Record
    public record SystemMetric(String metricName, double value, long timestamp) {}

    // Observer Interface
    public interface MetricsObserver {
        void onMetricPublished(SystemMetric metric);
    }

    // Subject (Publisher)
    public static class MetricsPublisher {
        private final List<MetricsObserver> observers = new ArrayList<>();

        public void subscribe(MetricsObserver observer) {
            observers.add(observer);
        }

        public void unsubscribe(MetricsObserver observer) {
            observers.remove(observer);
        }

        public void publishMetric(String metricName, double value) {
            SystemMetric metric = new SystemMetric(metricName, value, System.currentTimeMillis());
            for (MetricsObserver observer : observers) {
                observer.onMetricPublished(metric);
            }
        }

        public int getObserverCount() {
            return observers.size();
        }
    }

    // Concrete Observer 1: Alerting Engine
    public static class AlertingEngineObserver implements MetricsObserver {
        private final double threshold;
        private int alertCount = 0;

        public AlertingEngineObserver(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public void onMetricPublished(SystemMetric metric) {
            if (metric.value() > threshold) {
                alertCount++;
                System.out.printf("   [ALERTING OBSERVER] ALERT: Metric %s exceeded threshold %.2f (Current: %.2f)\n",
                        metric.metricName(), threshold, metric.value());
            }
        }

        public int getAlertCount() { return alertCount; }
    }

    // Concrete Observer 2: Grafana Dashboard Exporter
    public static class GrafanaExporterObserver implements MetricsObserver {
        private int metricsExported = 0;

        @Override
        public void onMetricPublished(SystemMetric metric) {
            metricsExported++;
        }

        public int getMetricsExported() { return metricsExported; }
    }
}
