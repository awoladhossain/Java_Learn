package com.example.buildtools;

import java.util.ArrayList;
import java.util.List;

/**
 * 🛠️ Phase 7.3: Maven Build Lifecycle & Dependency Scope Analyzer
 * 
 * Programmatically simulates and analyzes Maven build phases, plugin goal bindings,
 * dependency scope resolution, transitive dependency propagation, and exclusion rules.
 */
public class MavenLifecycleAnalyzer {

    // Maven Build Phase representation
    public enum MavenPhase {
        VALIDATE("validate", "Validates project structure and required metadata"),
        COMPILE("compile", "Compiles main source code under src/main/java to target/classes"),
        TEST("test", "Compiles and executes unit test cases under src/test/java using Surefire"),
        PACKAGE("package", "Packages compiled code into distribution format (JAR / WAR)"),
        VERIFY("verify", "Runs integration tests and quality checks (Failsafe plugin)"),
        INSTALL("install", "Installs package into local repository (~/.m2/repository)"),
        DEPLOY("deploy", "Copies final package to remote repository (Nexus / JFrog Artifactory)");

        private final String phaseName;
        private final String description;

        MavenPhase(String phaseName, String description) {
            this.phaseName = phaseName;
            this.description = description;
        }

        public String getPhaseName() { return phaseName; }
        public String getDescription() { return description; }
    }

    // Maven Dependency Scopes
    public enum DependencyScope {
        COMPILE(true, true, true, "Included in compile, test, and runtime classpaths. Transitive by default."),
        PROVIDED(true, true, false, "Included in compile and test classpaths, but NOT packaged at runtime (Provided by JDK/container)."),
        RUNTIME(false, true, true, "NOT needed for compilation, but included in test and runtime classpaths (e.g. JDBC Drivers)."),
        TEST(false, true, false, "Included ONLY in test compilation and execution classpaths. Never packaged."),
        SYSTEM(true, true, false, "Explicit path provided via <systemPath>. Discouraged in modern Maven."),
        IMPORT(false, false, false, "Used in <dependencyManagement> to import BOM (Bill of Materials) dependencies.");

        private final boolean compileClasspath;
        private final boolean testClasspath;
        private final boolean runtimeClasspath;
        private final String explanation;

        DependencyScope(boolean compileClasspath, boolean testClasspath, boolean runtimeClasspath, String explanation) {
            this.compileClasspath = compileClasspath;
            this.testClasspath = testClasspath;
            this.runtimeClasspath = runtimeClasspath;
            this.explanation = explanation;
        }

        public boolean isOnCompileClasspath() { return compileClasspath; }
        public boolean isOnTestClasspath() { return testClasspath; }
        public boolean isOnRuntimeClasspath() { return runtimeClasspath; }
        public String getExplanation() { return explanation; }
    }

    // Dependency representation
    public record MavenDependency(String groupId, String artifactId, String version, DependencyScope scope, List<String> exclusions) {
        public MavenDependency(String groupId, String artifactId, String version, DependencyScope scope) {
            this(groupId, artifactId, version, scope, List.of());
        }
    }

    /**
     * Computes lifecycle sequence up to target phase
     */
    public List<MavenPhase> getLifecycleSequence(MavenPhase targetPhase) {
        List<MavenPhase> sequence = new ArrayList<>();
        for (MavenPhase phase : MavenPhase.values()) {
            sequence.add(phase);
            if (phase == targetPhase) break;
        }
        return sequence;
    }

    /**
     * Resolves effective transitive scope given direct dependency scope and transitive scope.
     * Follows official Apache Maven Dependency Scope Transitivity Matrix.
     */
    public DependencyScope resolveTransitiveScope(DependencyScope directScope, DependencyScope transitiveScope) {
        if (transitiveScope == DependencyScope.PROVIDED || transitiveScope == DependencyScope.TEST) {
            return null; // Provided and Test dependencies are NEVER transitive!
        }

        if (directScope == DependencyScope.COMPILE) {
            return transitiveScope;
        } else if (directScope == DependencyScope.PROVIDED) {
            return (transitiveScope == DependencyScope.COMPILE || transitiveScope == DependencyScope.RUNTIME)
                    ? DependencyScope.PROVIDED : null;
        } else if (directScope == DependencyScope.RUNTIME) {
            return (transitiveScope == DependencyScope.COMPILE || transitiveScope == DependencyScope.RUNTIME)
                    ? DependencyScope.RUNTIME : null;
        } else if (directScope == DependencyScope.TEST) {
            return (transitiveScope == DependencyScope.COMPILE || transitiveScope == DependencyScope.RUNTIME)
                    ? DependencyScope.TEST : null;
        }
        return null;
    }

    /**
     * Evaluates conflict resolution: Maven uses "Nearest Definition" rule in the dependency tree.
     */
    public String resolveDependencyConflict(int depthA, String versionA, int depthB, String versionB) {
        if (depthA < depthB) {
            return String.format("Resolved to version %s (Depth %d < Depth %d)", versionA, depthA, depthB);
        } else if (depthB < depthA) {
            return String.format("Resolved to version %s (Depth %d < Depth %d)", versionB, depthB, depthA);
        } else {
            // First declared wins if depth is identical
            return String.format("Resolved to version %s (First declared at depth %d)", versionA, depthA);
        }
    }
}
