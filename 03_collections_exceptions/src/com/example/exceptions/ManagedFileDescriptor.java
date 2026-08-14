package com.example.exceptions;

/**
 * Simulates a system file descriptor or socket stream resource implementing AutoCloseable.
 * 
 * Demonstrates:
 * 1. LIFO (Last-In, First-Out) resource closing semantics in try-with-resources.
 * 2. AutoCloseable vs java.io.Closeable (Closeable limits close() to IOException, AutoCloseable allows Exception).
 */
public class ManagedFileDescriptor implements AutoCloseable {

    private final String fileName;
    private boolean active;

    public ManagedFileDescriptor(String fileName) {
        this.fileName = fileName;
        this.active = true;
        System.out.println("   [📁 FILE OPENED] File descriptor acquired: " + fileName);
    }

    public void writeData(String data) {
        if (!active) {
            throw new IllegalStateException("Attempted write to closed file descriptor: " + fileName);
        }
        System.out.println("   [📝 WRITING DATA] Writing to " + fileName + ": \"" + data + "\"");
    }

    @Override
    public void close() throws Exception {
        if (!active) {
            return;
        }
        this.active = false;
        System.out.println("   [🔒 FILE CLOSED] File descriptor released: " + fileName);
    }
}
