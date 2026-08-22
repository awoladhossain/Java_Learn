package com.example.loom;

import java.lang.ScopedValue;

/**
 * Section 5.4.3: Scoped Values (JEP 446 Preview).
 * 
 * Demonstrates:
 * - ScopedValue<T> as a modern, immutable replacement for ThreadLocal<T>.
 * - Bound execution using ScopedValue.where(KEY, value).run(...) / .call(...).
 * - Passing request telemetry context (traceId, userId) cleanly through call stacks.
 * - SRE Benefits: Zero memory leaks, immutability, sub-microsecond virtual thread inheritance.
 */
public class ScopedValuesDemo {

    // Domain record representing request context
    public record RequestContext(String requestId, String tenantId, String userRole) {}

    // Static Scoped Value declaration
    public static final ScopedValue<RequestContext> CURRENT_REQUEST = ScopedValue.newInstance();

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.4.3 SCOPED VALUES (JEP 446 Preview - ThreadLocal Replacement)");
        System.out.println("------------------------------------------------------------------------");

        RequestContext ctx1 = new RequestContext("req-9001", "tenant-alpha", "ROLE_ADMIN");
        RequestContext ctx2 = new RequestContext("req-9002", "tenant-beta", "ROLE_USER");

        System.out.println("--- 1. Executing Layered Pipeline with Bound Scoped Value (Context 1) ---");
        // Binding CURRENT_REQUEST to ctx1 for the duration of the runnable execution block
        ScopedValue.where(CURRENT_REQUEST, ctx1).run(() -> {
            ControllerLayer.handleRequest();
        });

        System.out.println("\n--- 2. Executing Layered Pipeline with Bound Scoped Value (Context 2) ---");
        ScopedValue.where(CURRENT_REQUEST, ctx2).run(() -> {
            ControllerLayer.handleRequest();
        });

        System.out.println("\n--- 3. Verifying Scope Lifetime & Unbound Safety ---");
        System.out.println("Is CURRENT_REQUEST bound outside scope block? " + CURRENT_REQUEST.isBound());

        System.out.println("\n💡 Senior SRE Scoped Values vs ThreadLocal Comparison:");
        System.out.println("   1. ThreadLocal variables are MUTABLE and UNBOUNDED, risking memory leaks if thread pool threads are reused without threadLocal.remove().");
        System.out.println("   2. ScopedValue is IMMUTABLE and BOUND strictly to the execution block lifetime—automatically cleared upon scope exit!");
        System.out.println("   3. ScopedValue is lightweight and highly optimized for millions of concurrent Virtual Threads.");
    }

    // Simulated Controller Layer
    public static class ControllerLayer {
        public static void handleRequest() {
            if (CURRENT_REQUEST.isBound()) {
                RequestContext ctx = CURRENT_REQUEST.get();
                System.out.printf("   [CONTROLLER] Processing request [%s] for tenant [%s]\n", ctx.requestId(), ctx.tenantId());
                ServiceLayer.executeBusinessLogic();
            }
        }
    }

    // Simulated Service Layer
    public static class ServiceLayer {
        public static void executeBusinessLogic() {
            RequestContext ctx = CURRENT_REQUEST.get();
            System.out.printf("   [SERVICE] Authorizing role [%s] for user request [%s]\n", ctx.userRole(), ctx.requestId());
            RepositoryLayer.executeQuery();
        }
    }

    // Simulated Repository Layer
    public static class RepositoryLayer {
        public static void executeQuery() {
            RequestContext ctx = CURRENT_REQUEST.get();
            System.out.printf("   [REPOSITORY] Executing SQL Query with Tenant Schema [%s_db]\n", ctx.tenantId());
        }
    }
}
