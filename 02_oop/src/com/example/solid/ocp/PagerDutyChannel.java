package com.example.solid.ocp;

public class PagerDutyChannel implements NotificationChannel {

    private final String serviceKey;

    public PagerDutyChannel(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    @Override
    public void sendNotification(String message) {
        System.out.printf("      🚨 [OCP - PagerDutyChannel] Triggering incident on key %s: %s%n",
                serviceKey, message);
    }

    @Override
    public String getChannelName() {
        return "PagerDuty (" + serviceKey + ")";
    }
}
