package com.example.interfaces;

/**
 * Interface 1 for Multiple Implementation testing.
 * Contains default method 'getStatusSummary()' which will collide with AuditLogger.
 */
public interface AlertNotifier {

    void sendAlert(String severity, String message);

    default String getStatusSummary() {
        return "AlertNotifier: Active channels [PagerDuty, Slack, Email]";
    }

    default String getNotificationChannel() {
        return "PagerDuty";
    }
}
