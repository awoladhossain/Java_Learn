package com.example.patterns.creational;

/**
 * 🛠️ Creational Pattern: Factory Method
 * 
 * Defines an interface for creating an object, but lets subclasses decide which class to instantiate.
 * Decouples client code from concrete product classes.
 */
public class FactoryMethodPattern {

    // Product Interface
    public interface Notification {
        String send(String recipient, String message);
    }

    // Concrete Product A
    public static class EmailNotification implements Notification {
        @Override
        public String send(String recipient, String message) {
            return String.format("[EMAIL] Sent to %s: %s", recipient, message);
        }
    }

    // Concrete Product B
    public static class SmsNotification implements Notification {
        @Override
        public String send(String recipient, String message) {
            return String.format("[SMS] Sent to %s: %s", recipient, message);
        }
    }

    // Concrete Product C
    public static class PushNotification implements Notification {
        @Override
        public String send(String recipient, String message) {
            return String.format("[PUSH] Sent to %s: %s", recipient, message);
        }
    }

    // Creator Abstract Base Class
    public static abstract class NotificationFactory {
        // Factory Method to be overridden by concrete creators
        public abstract Notification createNotification();

        // Business logic operating on the abstract Product
        public String notifyUser(String recipient, String message) {
            Notification notification = createNotification();
            return notification.send(recipient, message);
        }
    }

    // Concrete Creator A
    public static class EmailNotificationFactory extends NotificationFactory {
        @Override
        public Notification createNotification() {
            return new EmailNotification();
        }
    }

    // Concrete Creator B
    public static class SmsNotificationFactory extends NotificationFactory {
        @Override
        public Notification createNotification() {
            return new SmsNotification();
        }
    }

    // Concrete Creator C
    public static class PushNotificationFactory extends NotificationFactory {
        @Override
        public Notification createNotification() {
            return new PushNotification();
        }
    }
}
