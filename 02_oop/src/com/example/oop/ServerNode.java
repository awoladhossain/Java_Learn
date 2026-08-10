package com.example.oop;

/**
 * Domain Base Class illustrating State, Behavior, Constructor types, 'this' keyword,
 * static class-level memory layout, and 'final' immutability bounds.
 */
public class ServerNode {

    // Static Variable: Stored at Class level in Metaspace/Heap Class object metadata (shared by all instances)
    private static int totalNodeCount = 0;
    public static final String DEFAULT_REGION = "us-east-1";

    // Instance Fields (State): Stored inside each instance on the Heap
    private final String nodeId;        // Blank final: must be initialized in constructor
    private String hostname;
    private int port;
    private double cpuUsage;
    private boolean active;

    /**
     * 1️⃣ Default / No-Arg Constructor
     * Demonstrates Constructor Chaining using 'this(...)'
     */
    public ServerNode() {
        this("node-default-0", "127.0.0.1", 8080);
    }

    /**
     * 2️⃣ Parameterized Constructor
     * Demonstrates 'this' keyword to resolve instance field shadowing by parameters.
     */
    public ServerNode(String nodeId, String hostname, int port) {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.port = port;
        this.cpuUsage = 0.0;
        this.active = true;

        // Increment static class counter
        totalNodeCount++;
    }

    /**
     * 3️⃣ Copy Constructor
     * Creates a new ServerNode instance by copying state from an existing instance.
     */
    public ServerNode(ServerNode other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy from a null ServerNode instance");
        }
        this.nodeId = other.nodeId + "-copy";
        this.hostname = other.hostname;
        this.port = other.port;
        this.cpuUsage = other.cpuUsage;
        this.active = other.active;

        totalNodeCount++;
    }

    // Behavior Methods (Operations on State)
    public void updateCpuUsage(double cpuUsage) {
        if (cpuUsage < 0.0 || cpuUsage > 100.0) {
            throw new IllegalArgumentException("CPU usage percentage must be between 0.0 and 100.0");
        }
        this.cpuUsage = cpuUsage;
    }

    /**
     * Final method: Cannot be overridden by child classes in inheritance hierarchies.
     */
    public final String getNodeId() {
        return this.nodeId;
    }

    public String getHostname() { return hostname; }
    public int getPort() { return port; }
    public double getCpuUsage() { return cpuUsage; }
    public boolean isActive() { return active; }

    public static int getTotalNodeCount() {
        return totalNodeCount;
    }

    public String getNodeSummary() {
        return String.format("[ID: %s | Host: %s:%d | CPU: %.1f%% | Active: %b]",
                nodeId, hostname, port, cpuUsage, active);
    }
}
