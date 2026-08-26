package com.example.patterns.behavioral;

/**
 * 🛠️ Behavioral Pattern: Template Method
 * 
 * Defines the skeleton of an algorithm in an operation, deferring some steps to subclasses.
 * Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure.
 * E.g., Data Ingestion / ETL pipeline skeleton.
 */
public class TemplateMethodPattern {

    // Abstract Class defining Template Method
    public static abstract class DataIngestionPipeline {

        // Template method defining invariant execution sequence (marked final)
        public final String runPipeline(String sourceUri) {
            StringBuilder log = new StringBuilder();
            log.append(extract(sourceUri)).append(" | ");
            log.append(transform()).append(" | ");
            log.append(validate()).append(" | ");
            log.append(load());

            if (shouldSendMetricsNotification()) { // Hook method
                log.append(" | ").append(sendMetricsNotification());
            }

            return log.toString();
        }

        protected abstract String extract(String sourceUri);
        protected abstract String transform();
        protected abstract String validate();
        protected abstract String load();

        // Hook method (optional override)
        protected boolean shouldSendMetricsNotification() {
            return true;
        }

        protected String sendMetricsNotification() {
            return "ETL Metrics recorded";
        }
    }

    // Concrete Implementation 1: JSON Pipeline
    public static class JsonDataPipeline extends DataIngestionPipeline {
        @Override
        protected String extract(String sourceUri) {
            return "Extracted raw JSON payload from " + sourceUri;
        }

        @Override
        protected String transform() {
            return "Parsed JSON into Domain DTOs";
        }

        @Override
        protected String validate() {
            return "Validated Schema Constraints";
        }

        @Override
        protected String load() {
            return "Persisted DTOs to Database";
        }
    }

    // Concrete Implementation 2: CSV Pipeline
    public static class CsvDataPipeline extends DataIngestionPipeline {
        @Override
        protected String extract(String sourceUri) {
            return "Read CSV lines from " + sourceUri;
        }

        @Override
        protected String transform() {
            return "Converted CSV rows to columnar vectors";
        }

        @Override
        protected String validate() {
            return "Checked for missing column values";
        }

        @Override
        protected String load() {
            return "Bulk loaded into Data Warehouse";
        }

        @Override
        protected boolean shouldSendMetricsNotification() {
            return false; // Skip metrics notification for bulk CSV
        }
    }
}
