package com.example.solid.ocp;

/**
 * OCP (Open/Closed Principle):
 * System is OPEN for extension (by creating new NotificationChannel implementations)
 * but CLOSED for modification (NotificationService code never changes when adding new channels).
 */
public interface NotificationChannel {
    void sendNotification(String message);
    String getChannelName();
}
