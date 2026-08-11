package com.example.solid.ocp;

import java.util.List;

/**
 * High-level orchestration class adhering to Open/Closed Principle.
 * CLOSED for modification: We never modify this class when adding Email, Webhook, or Discord channels.
 * OPEN for extension: New channels implement NotificationChannel interface and pass in seamlessly.
 */
public class NotificationService {

    private final List<NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public void dispatchAlert(String alertMessage) {
        System.out.printf("   🔔 [OCP] Dispatching alert across %d configured channels:%n", channels.size());
        for (NotificationChannel channel : channels) {
            channel.sendNotification(alertMessage);
        }
    }
}
