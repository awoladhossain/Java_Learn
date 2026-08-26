package com.example.buildtools;

import java.util.*;

/**
 * 🛠️ Phase 7.3: Gradle Task Execution & Multi-Module Graph Analyzer
 * 
 * Programmatically simulates and analyzes Gradle build phases (Initialization, Configuration, Execution),
 * Directed Acyclic Graph (DAG) task dependencies, incremental task execution, and module dependency leakage (api vs implementation).
 */
public class GradleTaskGraphAnalyzer {

    // Gradle Build Phases
    public enum GradlePhase {
        INITIALIZATION("Initialization Phase: Evaluates settings.gradle, determines participating projects"),
        CONFIGURATION("Configuration Phase: Executes build.gradle scripts, constructs Task DAG dependency tree"),
        EXECUTION("Execution Phase: Executes scheduled tasks in topological DAG order based on inputs/outputs");

        private final String description;

        GradlePhase(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    // Gradle Configuration scopes
    public enum GradleConfigurationScope {
        API("api", "Exposes dependency transitively to consuming modules (java-library plugin). Use sparingly."),
        IMPLEMENTATION("implementation", "Internal dependency. Does NOT leak to consumers, reducing re-compilation ripple."),
        COMPILE_ONLY("compileOnly", "Needed during compilation only (e.g. Lombok annotations). Not packaged."),
        RUNTIME_ONLY("runtimeOnly", "Needed only during runtime execution (e.g. H2/PostgreSQL drivers)."),
        ANNOTATION_PROCESSOR("annotationProcessor", "Code generation annotation processors."),
        TEST_IMPLEMENTATION("testImplementation", "Dependencies required for test source compilation."),
        TEST_RUNTIME_ONLY("testRuntimeOnly", "Dependencies required for test execution runtime.");

        private final String configurationName;
        private final String description;

        GradleConfigurationScope(String configurationName, String description) {
            this.configurationName = configurationName;
            this.description = description;
        }

        public String getConfigurationName() { return configurationName; }
        public String getDescription() { return description; }
    }

    // Gradle Task Node
    public static class GradleTask {
        private final String name;
        private final List<String> dependencies;
        private boolean upToDate;

        public GradleTask(String name, List<String> dependencies) {
            this.name = name;
            this.dependencies = new ArrayList<>(dependencies);
            this.upToDate = false;
        }

        public String getName() { return name; }
        public List<String> getDependencies() { return dependencies; }
        public boolean isUpToDate() { return upToDate; }
        public void setUpToDate(boolean upToDate) { this.upToDate = upToDate; }
    }

    // Task DAG Engine
    public static class TaskGraphEngine {
        private final Map<String, GradleTask> taskMap = new LinkedHashMap<>();

        public void addTask(GradleTask task) {
            taskMap.put(task.getName(), task);
        }

        /**
         * Resolves topological execution order of tasks based on DAG dependencies
         */
        public List<String> resolveExecutionPlan(String targetTaskName) {
            List<String> executionPlan = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> stateVisiting = new HashSet<>();

            topologicalSort(targetTaskName, visited, stateVisiting, executionPlan);
            return executionPlan;
        }

        private void topologicalSort(String taskName, Set<String> visited, Set<String> stateVisiting, List<String> executionPlan) {
            if (stateVisiting.contains(taskName)) {
                throw new IllegalStateException("Circular task dependency detected in Gradle DAG: " + taskName);
            }
            if (!visited.contains(taskName)) {
                stateVisiting.add(taskName);
                GradleTask task = taskMap.get(taskName);
                if (task != null) {
                    for (String dep : task.getDependencies()) {
                        topologicalSort(dep, visited, stateVisiting, executionPlan);
                    }
                }
                stateVisiting.remove(taskName);
                visited.add(taskName);
                executionPlan.add(taskName);
            }
        }
    }
}
