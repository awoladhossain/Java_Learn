package com.example.patterns.creational;

import java.io.Serializable;

/**
 * 🛠️ Creational Pattern: Singleton
 * 
 * Demonstrates thread-safe Singleton implementations in Java:
 * 1. Double-Checked Locking Singleton with 'volatile' keyword.
 * 2. Enum Singleton (Joshua Bloch's recommended approach - thread-safe, serialization-proof, reflection-proof).
 */
public class SingletonPattern {

    /**
     * 1. Double-Checked Locking Singleton
     * 
     * Uses volatile variable to prevent instruction reordering hazards in JMM.
     * Prevents multiple threads from instantiating duplicate instances during lazy initialization.
     */
    public static class DoubleCheckedLockingSingleton implements Serializable {
        private static final long serialVersionUID = 1L;

        // Volatile ensures visible writes across threads and prevents instruction reordering
        private static volatile DoubleCheckedLockingSingleton instance;

        private final String configurationId;

        // Private constructor prevents instantiation from other packages
        private DoubleCheckedLockingSingleton() {
            // Protect against reflection attack
            if (instance != null) {
                throw new IllegalStateException("Instance already created! Use getInstance().");
            }
            this.configurationId = "CONFIG-DCL-" + System.nanoTime();
        }

        public static DoubleCheckedLockingSingleton getInstance() {
            if (instance == null) { // First check (no locking overhead)
                synchronized (DoubleCheckedLockingSingleton.class) {
                    if (instance == null) { // Second check (with locking)
                        instance = new DoubleCheckedLockingSingleton();
                    }
                }
            }
            return instance;
        }

        public String getConfigurationId() {
            return configurationId;
        }

        // Serialization protection: return existing instance during deserialization
        protected Object readResolve() {
            return getInstance();
        }
    }

    /**
     * 2. Enum Singleton
     * 
     * Recommended by Effective Java (Item 3).
     * Automatically handles thread safety, reflection safety (JVM forbids Enum instantiation via reflection),
     * and serialization safety guarantees out of the box.
     */
    public enum EnumSingleton {
        INSTANCE;

        private final String connectionPoolId;

        EnumSingleton() {
            this.connectionPoolId = "ENUM-POOL-" + System.nanoTime();
        }

        public String getConnectionPoolId() {
            return connectionPoolId;
        }

        public void executeTask(String taskName) {
            // Simulates thread-safe operation using Enum Singleton
        }
    }
}
