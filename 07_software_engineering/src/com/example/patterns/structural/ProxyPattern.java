package com.example.patterns.structural;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 🛠️ Structural Pattern: Proxy (Dynamic Proxy via java.lang.reflect.Proxy)
 * 
 * Provides a surrogate or placeholder for another object to control access to it.
 * Uses JDK Dynamic Proxies to dynamically intercept method execution at runtime,
 * enforcing security, audit logging, and latency telemetry (SRE tracing).
 */
public class ProxyPattern {

    // Subject Interface
    public interface OrderService {
        String processOrder(String orderId, double amount);
        String cancelOrder(String orderId);
    }

    // Real Subject
    public static class RealOrderService implements OrderService {
        @Override
        public String processOrder(String orderId, double amount) {
            return String.format("Order %s processed successfully for $%.2f", orderId, amount);
        }

        @Override
        public String cancelOrder(String orderId) {
            return String.format("Order %s cancelled successfully", orderId);
        }
    }

    // InvocationHandler for Dynamic Proxy
    public static class AuditAndLatencyInvocationHandler implements InvocationHandler {
        private final Object target;
        private int invocationCount = 0;

        public AuditAndLatencyInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            invocationCount++;
            long startNs = System.nanoTime();

            // SRE Audit Log
            String methodName = method.getName();

            // Execute target method dynamically via Reflection
            Object result = method.invoke(target, args);

            long durationNs = System.nanoTime() - startNs;
            // Trace instrumentation note
            System.out.printf("   [DYNAMIC PROXY AUDIT] Method: %s | Execution Time: %d ns | Invocation #%d\n",
                    methodName, durationNs, invocationCount);

            return result;
        }

        public int getInvocationCount() {
            return invocationCount;
        }
    }

    /**
     * Factory helper to create dynamic proxy instance targeting OrderService interface
     */
    public static OrderService createProxy(OrderService realService, AuditAndLatencyInvocationHandler handler) {
        return (OrderService) Proxy.newProxyInstance(
                OrderService.class.getClassLoader(),
                new Class<?>[]{OrderService.class},
                handler
        );
    }
}
