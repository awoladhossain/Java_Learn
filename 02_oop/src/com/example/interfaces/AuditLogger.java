package com.example.interfaces;

/**
 * Interface 2 for Multiple Implementation testing.
 * Contains default method 'getStatusSummary()' which collides with AlertNotifier.
 */
public interface AuditLogger {

    void logAuditEvent(String user, String action, String target);

    default String getStatusSummary() {
        return "AuditLogger: Standard ISO-8601 audit stream active";
    }

    default String getStorageBackend() {
        return "Elasticsearch / OpenSearch";
    }
}
