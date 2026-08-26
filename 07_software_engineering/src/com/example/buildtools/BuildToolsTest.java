package com.example.buildtools;

import com.example.buildtools.MavenLifecycleAnalyzer.*;
import com.example.buildtools.GradleTaskGraphAnalyzer.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 7.3: Build Tools Mastery (Maven & Gradle) Test Suite")
public class BuildToolsTest {

    @Nested
    @DisplayName("Maven Build Mechanics Tests")
    class MavenMechanicsTest {

        private final MavenLifecycleAnalyzer mavenAnalyzer = new MavenLifecycleAnalyzer();

        @Test
        @DisplayName("Maven: Verifies lifecycle phase sequence up to 'test'")
        void testLifecycleSequence() {
            List<MavenPhase> phases = mavenAnalyzer.getLifecycleSequence(MavenPhase.TEST);
            assertEquals(3, phases.size());
            assertEquals(MavenPhase.VALIDATE, phases.get(0));
            assertEquals(MavenPhase.COMPILE, phases.get(1));
            assertEquals(MavenPhase.TEST, phases.get(2));
        }

        @Test
        @DisplayName("Maven: Verifies dependency scope classpath visibility rules")
        void testScopeVisibility() {
            assertTrue(DependencyScope.COMPILE.isOnCompileClasspath());
            assertTrue(DependencyScope.COMPILE.isOnRuntimeClasspath());

            assertTrue(DependencyScope.PROVIDED.isOnCompileClasspath());
            assertFalse(DependencyScope.PROVIDED.isOnRuntimeClasspath());

            assertFalse(DependencyScope.RUNTIME.isOnCompileClasspath());
            assertTrue(DependencyScope.RUNTIME.isOnRuntimeClasspath());

            assertFalse(DependencyScope.TEST.isOnCompileClasspath());
            assertFalse(DependencyScope.TEST.isOnRuntimeClasspath());
            assertTrue(DependencyScope.TEST.isOnTestClasspath());
        }

        @Test
        @DisplayName("Maven: Resolves transitive scope rules correctly")
        void testTransitiveScopeResolution() {
            // Direct Compile + Transitive Runtime = Runtime
            DependencyScope scope1 = mavenAnalyzer.resolveTransitiveScope(DependencyScope.COMPILE, DependencyScope.RUNTIME);
            assertEquals(DependencyScope.RUNTIME, scope1);

            // Direct Compile + Transitive Test = null (Test scope dependencies are never transitive)
            DependencyScope scope2 = mavenAnalyzer.resolveTransitiveScope(DependencyScope.COMPILE, DependencyScope.TEST);
            assertNull(scope2);

            // Direct Test + Transitive Compile = Test
            DependencyScope scope3 = mavenAnalyzer.resolveTransitiveScope(DependencyScope.TEST, DependencyScope.COMPILE);
            assertEquals(DependencyScope.TEST, scope3);
        }

        @Test
        @DisplayName("Maven: Evaluates Nearest Definition conflict resolution rule")
        void testConflictResolution() {
            String result = mavenAnalyzer.resolveDependencyConflict(2, "2.0.0", 3, "1.0.0");
            assertTrue(result.contains("Resolved to version 2.0.0"));
            assertTrue(result.contains("Depth 2 < Depth 3"));
        }
    }

    @Nested
    @DisplayName("Gradle Task Graph & Multi-Module Tests")
    class GradleMechanicsTest {

        @Test
        @DisplayName("Gradle: Topological DAG task execution plan computation")
        void testTaskGraphDAG() {
            TaskGraphEngine engine = new TaskGraphEngine();
            engine.addTask(new GradleTask("compileJava", List.of()));
            engine.addTask(new GradleTask("classes", List.of("compileJava")));
            engine.addTask(new GradleTask("jar", List.of("classes")));

            List<String> plan = engine.resolveExecutionPlan("jar");
            assertEquals(3, plan.size());
            assertEquals("compileJava", plan.get(0));
            assertEquals("classes", plan.get(1));
            assertEquals("jar", plan.get(2));
        }

        @Test
        @DisplayName("Gradle: Detects circular task dependency cycles")
        void testCircularTaskDependencyDetection() {
            TaskGraphEngine engine = new TaskGraphEngine();
            engine.addTask(new GradleTask("taskA", List.of("taskB")));
            engine.addTask(new GradleTask("taskB", List.of("taskA")));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> engine.resolveExecutionPlan("taskA"));
            assertTrue(ex.getMessage().contains("Circular task dependency detected"));
        }

        @Test
        @DisplayName("Gradle: Verifies configuration scope specifications")
        void testConfigurationScopes() {
            assertEquals("api", GradleConfigurationScope.API.getConfigurationName());
            assertEquals("implementation", GradleConfigurationScope.IMPLEMENTATION.getConfigurationName());
            assertTrue(GradleConfigurationScope.API.getDescription().contains("transitively"));
            assertTrue(GradleConfigurationScope.IMPLEMENTATION.getDescription().contains("Does NOT leak"));
        }
    }
}
