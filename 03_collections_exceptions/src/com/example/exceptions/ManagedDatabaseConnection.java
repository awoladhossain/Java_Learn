package com.example.exceptions;

/**
 * Simulates a managed database connection implementing AutoCloseable.
 * 
 * Used to demonstrate:
 * 1. Automatic resource cleanup with try-with-resources.
 * 2. File descriptor and connection leak prevention.
 * 3. Throwing exceptions during close() to demonstrate suppressed exception propagation.
 */
public class ManagedDatabaseConnection implements AutoCloseable {

    private final String connectionId;
    private boolean open;
    private boolean failOnClose;

    public ManagedDatabaseConnection(String connectionId) {
        this.connectionId = connectionId;
        this.open = true;
        this.failOnClose = false;
        System.out.println("   [🔌 RESOURCE ALLOCATED] Database connection opened: " + connectionId);
    }

    public void setFailOnClose(boolean failOnClose) {
        this.failOnClose = failOnClose;
    }

    public void executeQuery(String query) {
        if (!open) {
            throw new IllegalStateException("Cannot execute query on closed connection: " + connectionId);
        }
        System.out.println("   [⚡ EXECUTING QUERY] Connection " + connectionId + " -> " + query);
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws Exception {
        if (!open) {
            System.out.println("   [⚠️ ALREADY CLOSED] Connection " + connectionId + " was already closed.");
            return;
        }
        this.open = false;

        if (failOnClose) {
            System.out.println("   [💥 CLOSE ERROR] Connection " + connectionId + " failed during resource close cleanup!");
            throw new IllegalStateException("Failed to gracefully close network socket for connection: " + connectionId);
        }

        System.out.println("   [🔒 RESOURCE RELEASED] Database connection closed safely: " + connectionId);
    }
}
