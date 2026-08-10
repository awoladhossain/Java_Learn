package com.example.oop;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Class illustrating:
 * 1. Private Constructor (prevents external instantiation)
 * 2. Thread-Safe Bill Pugh Singleton Pattern (Static Inner Holder class)
 * 3. Static initialization block
 * 4. Constant final configuration properties
 */
public class ClusterConfigManager {

    // Global static final constants
    public static final String CLUSTER_NAME = "prod-k8s-cluster-alpha";
    public static final int MAX_CONNECTIONS;

    private final Map<String, String> configMap;

    // Static Initialization Block (Executes once when class is loaded into JVM Metaspace)
    static {
        MAX_CONNECTIONS = 500;
    }

    /**
     * Private Constructor: Prevents instantiation from outside the class.
     */
    private ClusterConfigManager() {
        this.configMap = new HashMap<>();
        // Load default cluster configurations
        configMap.put("timeout_ms", "5000");
        configMap.put("retry_count", "3");
        configMap.put("ssl_enabled", "true");
    }

    /**
     * Bill Pugh Singleton Holder (Thread-safe, Lazy Initialization without synchronized performance overhead)
     */
    private static class InstanceHolder {
        private static final ClusterConfigManager INSTANCE = new ClusterConfigManager();
    }

    /**
     * Global Access Point to the Singleton instance.
     */
    public static ClusterConfigManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public String getConfig(String key) {
        return configMap.get(key);
    }

    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }

    public Map<String, String> getAllConfigs() {
        return new HashMap<>(configMap);
    }
}
