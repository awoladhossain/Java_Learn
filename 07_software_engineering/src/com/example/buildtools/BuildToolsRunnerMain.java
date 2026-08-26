package com.example.buildtools;

import com.example.buildtools.MavenLifecycleAnalyzer.*;
import com.example.buildtools.GradleTaskGraphAnalyzer.*;

import java.util.List;

/**
 * 🚀 Main Executable Demonstration Runner for Phase 7.3: Build Tools Mastery (Maven & Gradle).
 * 
 * Programmatically demonstrates Maven Lifecycles, POM structures, dependency scopes, exclusions,
 * and Gradle task graphs, configuration scopes (api vs implementation), and multi-module mechanics.
 */
public class BuildToolsRunnerMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("📦 PHASE 7.3: BUILD TOOLS MASTERY (MAVEN & GRADLE)");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // ------------------------------------------------------------------------
        // 1. MAVEN LIFECYCLES, SCOPES & CONFLICT RESOLUTION
        // ------------------------------------------------------------------------
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("1️⃣ MAVEN BUILD MECHANICS & DEPENDENCY SCOPE MATRIX");
        System.out.println("------------------------------------------------------------------------");

        MavenLifecycleAnalyzer mavenAnalyzer = new MavenLifecycleAnalyzer();

        // Demonstrate Default Lifecycle sequence
        List<MavenPhase> packageSeq = mavenAnalyzer.getLifecycleSequence(MavenPhase.PACKAGE);
        System.out.println("   📌 Maven Default Lifecycle Phases up to 'package':");
        for (MavenPhase phase : packageSeq) {
            System.out.printf("      ├── %-10s : %s\n", phase.getPhaseName(), phase.getDescription());
        }

        // Demonstrate Dependency Scope Matrix
        System.out.println("\n   📌 Dependency Scope Classpath Matrix:");
        System.out.printf("      %-10s | %-8s | %-8s | %-8s | %s\n", "Scope", "Compile", "Test", "Runtime", "Transitive Rule");
        System.out.println("      ------------------------------------------------------------------");
        for (DependencyScope scope : DependencyScope.values()) {
            if (scope == DependencyScope.IMPORT) continue;
            System.out.printf("      %-10s | %-8b | %-8b | %-8b | %s\n",
                    scope.name(), scope.isOnCompileClasspath(), scope.isOnTestClasspath(),
                    scope.isOnRuntimeClasspath(), scope.getExplanation().substring(0, 35) + "...");
        }

        // Transitive Scope Resolution Simulation
        DependencyScope resolvedTransitive = mavenAnalyzer.resolveTransitiveScope(DependencyScope.COMPILE, DependencyScope.RUNTIME);
        DependencyScope testTransitive = mavenAnalyzer.resolveTransitiveScope(DependencyScope.COMPILE, DependencyScope.TEST);
        System.out.println("\n   📌 Transitive Resolution Simulation:");
        System.out.printf("      ├── [Direct: COMPILE] + [Transitive: RUNTIME] => Effective Scope: %s\n", resolvedTransitive);
        System.out.printf("      ├── [Direct: COMPILE] + [Transitive: TEST]    => Effective Scope: %s (Omitted! Test deps are non-transitive)\n", testTransitive);

        // Conflict Resolution Rule
        String conflictRes = mavenAnalyzer.resolveDependencyConflict(2, "2.0.0", 4, "1.5.0");
        System.out.printf("      ├── Conflict Resolution (Nearest Definition Rule): %s\n", conflictRes);

        // ------------------------------------------------------------------------
        // 2. GRADLE BUILD PHASES, TASK DAG & MULTI-MODULE ARCHITECTURE
        // ------------------------------------------------------------------------
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("2️⃣ GRADLE BUILD PHASES, TASK DAG & CONFIGURATION SCOPES");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   📌 Gradle Build Lifecycle Phases:");
        for (GradlePhase phase : GradlePhase.values()) {
            System.out.printf("      ├── %s\n", phase.getDescription());
        }

        // Gradle Configuration Scopes: api vs implementation
        System.out.println("\n   📌 Gradle Modern Dependency Configurations (api vs implementation):");
        for (GradleConfigurationScope configScope : GradleConfigurationScope.values()) {
            System.out.printf("      ├── %-22s : %s\n", configScope.getConfigurationName(), configScope.getDescription());
        }

        // Construct Gradle Task DAG Execution Graph
        TaskGraphEngine dagEngine = new TaskGraphEngine();
        dagEngine.addTask(new GradleTask("compileJava", List.of()));
        dagEngine.addTask(new GradleTask("processResources", List.of()));
        dagEngine.addTask(new GradleTask("classes", List.of("compileJava", "processResources")));
        dagEngine.addTask(new GradleTask("compileTestJava", List.of("classes")));
        dagEngine.addTask(new GradleTask("processTestResources", List.of()));
        dagEngine.addTask(new GradleTask("testClasses", List.of("compileTestJava", "processTestResources")));
        dagEngine.addTask(new GradleTask("test", List.of("testClasses")));
        dagEngine.addTask(new GradleTask("jar", List.of("classes")));
        dagEngine.addTask(new GradleTask("assemble", List.of("jar")));
        dagEngine.addTask(new GradleTask("build", List.of("assemble", "test")));

        List<String> executionPlan = dagEngine.resolveExecutionPlan("build");
        System.out.println("\n   📌 Computed Gradle DAG Execution Plan for task ':build':");
        System.out.print("      ");
        for (int i = 0; i < executionPlan.size(); i++) {
            System.out.print(":" + executionPlan.get(i));
            if (i < executionPlan.size() - 1) System.out.print(" -> ");
        }
        System.out.println();

        // Multi-Module Project Architecture
        System.out.println("\n   📌 Multi-Module Project Architecture Layout (multi-module-demo/):");
        System.out.println("      enterprise-multi-module/");
        System.out.println("      ├── settings.gradle        (include 'core', 'app')");
        System.out.println("      ├── build.gradle           (root allprojects / subprojects configuration)");
        System.out.println("      ├── core/");
        System.out.println("      │   └── build.gradle       (plugins { id 'java-library' }, api 'slf4j-api')");
        System.out.println("      └── app/");
        System.out.println("          └── build.gradle       (dependencies { implementation project(':core') })");

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("💡 SENIOR SRE BUILD SYSTEM GOLDEN RULES");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   1. Avoid Transitive Contamination: Always use <exclusions> in Maven or exclude in Gradle to prevent version drift.");
        System.out.println("   2. Leverage Gradle 'implementation' over 'api': Hides internal dependencies and speeds up incremental builds.");
        System.out.println("   3. Pin Toolchains Explicitly: Ensure java { toolchain { languageVersion = JavaLanguageVersion.of(21) } } is enforced.");
        System.out.println("   4. Hermetic Builds: Always commit 'mvnw' (Maven Wrapper) or 'gradlew' (Gradle Wrapper) to repository for deterministic CI/CD.");
        System.out.printf("======================================================================== [Completed in %d ms]\n", elapsedTime);
    }
}
