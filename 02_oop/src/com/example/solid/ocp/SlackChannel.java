package com.example.solid.ocp;

public class SlackChannel implements NotificationChannel {

    private final String webhookUrl;

    public SlackChannel(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void sendNotification(String message) {
        System.out.printf("      💬 [OCP - SlackChannel] Posting to %s: %s%n", webhookUrl, message);
    }

    @Override
    public String getChannelName() {
        return "Slack (" + webhookUrl + ")";
    }
}
