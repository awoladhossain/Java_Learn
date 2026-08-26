package com.example.patterns.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 🛠️ Behavioral Pattern: Command
 * 
 * Encapsulates a request as an object, thereby letting you parameterize clients with different requests,
 * queue or log requests, and support undoable operations.
 * E.g., SRE Infrastructure Control Plane supporting execution history and rollbacks.
 */
public class CommandPattern {

    // Command Interface
    public interface Command {
        String execute();
        String undo();
    }

    // Receiver: Server Infrastructure Instance
    public static class ServerInstance {
        private final String serverId;
        private boolean running = false;
        private int instancesCount = 1;

        public ServerInstance(String serverId) {
            this.serverId = serverId;
        }

        public String start() {
            this.running = true;
            return "Server " + serverId + " STARTED";
        }

        public String stop() {
            this.running = false;
            return "Server " + serverId + " STOPPED";
        }

        public String scale(int targetInstances) {
            int old = this.instancesCount;
            this.instancesCount = targetInstances;
            return String.format("Server %s scaled from %d to %d instances", serverId, old, targetInstances);
        }

        public boolean isRunning() { return running; }
        public int getInstancesCount() { return instancesCount; }
    }

    // Concrete Command 1: Start Server
    public static class StartServerCommand implements Command {
        private final ServerInstance server;

        public StartServerCommand(ServerInstance server) {
            this.server = server;
        }

        @Override
        public String execute() {
            return server.start();
        }

        @Override
        public String undo() {
            return server.stop();
        }
    }

    // Concrete Command 2: Scale Cluster
    public static class ScaleClusterCommand implements Command {
        private final ServerInstance server;
        private final int newCount;
        private int previousCount;

        public ScaleClusterCommand(ServerInstance server, int newCount) {
            this.server = server;
            this.newCount = newCount;
        }

        @Override
        public String execute() {
            this.previousCount = server.getInstancesCount();
            return server.scale(newCount);
        }

        @Override
        public String undo() {
            return server.scale(previousCount);
        }
    }

    // Invoker Class with Undo History Stack
    public static class ControlPlaneInvoker {
        private final Deque<Command> history = new ArrayDeque<>();

        public String executeCommand(Command command) {
            String result = command.execute();
            history.push(command);
            return result;
        }

        public String undoLastCommand() {
            if (history.isEmpty()) {
                return "NO COMMAND TO UNDO";
            }
            Command lastCommand = history.pop();
            return lastCommand.undo();
        }

        public int getHistoryDepth() {
            return history.size();
        }
    }
}
