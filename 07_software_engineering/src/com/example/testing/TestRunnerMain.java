package com.example.testing;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Main Executable Test Launcher for Phase 7.1: Unit & Integration Testing.
 * 
 * Programmatically executes JUnit 5, Mockito 5, and TDD test suites via JUnit Platform Launcher API,
 * outputting test metrics, execution counts, and Senior SRE Testing Guidelines.
 */
public class TestRunnerMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("🧪 PHASE 7.1: UNIT & INTEGRATION TESTING (JUNIT 5, MOCKITO & TDD)");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Build JUnit Platform Discovery Request targeting com.example.testing package
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.example.testing"))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        System.out.println("\n🚀 Discovering and executing JUnit 5 & Mockito test suites...");
        launcher.execute(request);

        // 2. Extract Test Summary Metrics
        TestExecutionSummary summary = listener.getSummary();
        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📊 TEST EXECUTION SUMMARY REPORT");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("   Tests Found      : %d\n", summary.getTestsFoundCount());
        System.out.printf("   Tests Started    : %d\n", summary.getTestsStartedCount());
        System.out.printf("   Tests Successful : %d\n", summary.getTestsSucceededCount());
        System.out.printf("   Tests Failed     : %d\n", summary.getTestsFailedCount());
        System.out.printf("   Tests Skipped    : %d\n", summary.getTestsSkippedCount());
        System.out.printf("   Execution Time   : %d ms\n", elapsedTime);

        if (!summary.getFailures().isEmpty()) {
            System.err.println("\n❌ TEST FAILURES ENCOUNTERED:");
            summary.getFailures().forEach(failure -> 
                System.err.printf("   ├── %s: %s\n", failure.getTestIdentifier().getDisplayName(), failure.getException().getMessage())
            );
        } else {
            System.out.println("\n🎉 ALL UNIT, MOCKITO & TDD TESTS PASSED WITH 100% SUCCESS RATE!");
        }

        // 3. Senior SRE Enterprise Testing Rules
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("💡 SENIOR SRE ENTERPRISE TESTING GOLDEN RULES");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   1. Fast Feedback SLA: Unit test suites MUST execute under 10 seconds total.");
        System.out.println("   2. Isolation Principle: Mock external IO, DB repositories, and HTTP network gateways.");
        System.out.println("   3. Zero Flaky Tests: Tests must be deterministic and isolated from ambient clock/thread timings.");
        System.out.println("   4. TDD discipline: Write failing test specs before writing domain code to drive clean architecture.");

        System.out.println("========================================================================\n");
    }
}
