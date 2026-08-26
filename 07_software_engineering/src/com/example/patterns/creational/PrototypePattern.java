package com.example.patterns.creational;

import java.util.HashMap;
import java.util.Map;

/**
 * 🛠️ Creational Pattern: Prototype
 * 
 * Specifies the kinds of objects to create using a prototypical instance, and creates new objects
 * by copying this prototype (deep cloning vs shallow cloning).
 * Useful when object creation is computationally expensive or requires loading heavy remote resources.
 */
public class PrototypePattern {

    public interface Prototype<T> {
        T clonePrototype();
    }

    public static class ServerConfiguration implements Prototype<ServerConfiguration>, Cloneable {
        private String environment;
        private int maxConnections;
        private Map<String, String> featureFlags;

        public ServerConfiguration(String environment, int maxConnections, Map<String, String> featureFlags) {
            this.environment = environment;
            this.maxConnections = maxConnections;
            // Defensive copy for initial map
            this.featureFlags = new HashMap<>(featureFlags);
        }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public Map<String, String> getFeatureFlags() { return featureFlags; }
        public void setFeatureFlag(String key, String value) { this.featureFlags.put(key, value); }

        /**
         * Deep cloning implementation to ensure full state isolation between original prototype
         * and cloned objects.
         */
        @Override
        public ServerConfiguration clonePrototype() {
            // Deep copy featureFlags map so mutations in clone don't corrupt prototype
            Map<String, String> clonedFlags = new HashMap<>(this.featureFlags);
            return new ServerConfiguration(this.environment, this.maxConnections, clonedFlags);
        }

        @Override
        public String toString() {
            return String.format("ServerConfiguration[env='%s', maxConn=%d, flags=%s]",
                    environment, maxConnections, featureFlags);
        }
    }
}
