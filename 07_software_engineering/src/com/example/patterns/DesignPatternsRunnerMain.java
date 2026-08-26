package com.example.patterns;

import com.example.patterns.creational.*;
import com.example.patterns.structural.*;
import com.example.patterns.behavioral.*;

import java.util.Map;

/**
 * 🚀 Main Executable Demonstration Runner for Phase 7.2: Gang of Four (GoF) Design Patterns.
 * 
 * Demonstrates Creational, Structural, and Behavioral patterns with Senior SRE commentary.
 */
public class DesignPatternsRunnerMain {

    public static void main(String[] args) throws Exception {
        System.out.println("========================================================================");
        System.out.println("🏗️ PHASE 7.2: SOFTWARE DESIGN PATTERNS (GANG OF FOUR - GoF)");
        System.out.println("========================================================================");

        long startOverall = System.currentTimeMillis();

        // ------------------------------------------------------------------------
        // 1. CREATIONAL PATTERNS
        // ------------------------------------------------------------------------
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("1️⃣ CREATIONAL DESIGN PATTERNS");
        System.out.println("------------------------------------------------------------------------");

        // Singleton (Double-Checked Locking & Enum)
        SingletonPattern.DoubleCheckedLockingSingleton dcl1 = SingletonPattern.DoubleCheckedLockingSingleton.getInstance();
        SingletonPattern.DoubleCheckedLockingSingleton dcl2 = SingletonPattern.DoubleCheckedLockingSingleton.getInstance();
        System.out.printf("   [Singleton DCL] Same instance check: %b (Config ID: %s)\n",
                (dcl1 == dcl2), dcl1.getConfigurationId());

        SingletonPattern.EnumSingleton enum1 = SingletonPattern.EnumSingleton.INSTANCE;
        SingletonPattern.EnumSingleton enum2 = SingletonPattern.EnumSingleton.INSTANCE;
        System.out.printf("   [Singleton Enum] Same enum instance check: %b (Pool ID: %s)\n",
                (enum1 == enum2), enum1.getConnectionPoolId());

        // Factory Method
        FactoryMethodPattern.NotificationFactory factory = new FactoryMethodPattern.EmailNotificationFactory();
        System.out.println("   [Factory Method] " + factory.notifyUser("user@example.com", "Server CPU usage > 90%"));

        // Abstract Factory
        AbstractFactoryPattern.CloudResourceFactory awsFactory = new AbstractFactoryPattern.AwsResourceFactory();
        AbstractFactoryPattern.ComputeInstance awsVm = awsFactory.createComputeInstance();
        AbstractFactoryPattern.StorageBucket awsBucket = awsFactory.createStorageBucket("sre-metrics-bucket");
        System.out.println("   [Abstract Factory AWS] " + awsVm.launch());
        System.out.println("   [Abstract Factory AWS] " + awsBucket.storeFile("heapdump.hprof"));

        // Builder (Fluent Interface)
        BuilderPattern.HttpRequest request = new BuilderPattern.HttpRequest.Builder("https://api.internal.net/v1/telemetry")
                .method("POST")
                .header("Authorization", "Bearer secret-token")
                .header("Content-Type", "application/json")
                .body("{\"metric\": \"latency_ms\", \"value\": 42}")
                .connectTimeoutMs(2000)
                .readTimeoutMs(5000)
                .build();
        System.out.println("   [Builder] Built Immutable HTTP Request: " + request);

        // Prototype
        PrototypePattern.ServerConfiguration protoConfig = new PrototypePattern.ServerConfiguration("production", 500, Map.of("canary", "true"));
        PrototypePattern.ServerConfiguration clonedConfig = protoConfig.clonePrototype();
        clonedConfig.setEnvironment("staging");
        clonedConfig.setFeatureFlag("canary", "false");
        System.out.println("   [Prototype Original] " + protoConfig);
        System.out.println("   [Prototype Cloned]   " + clonedConfig);

        // ------------------------------------------------------------------------
        // 2. STRUCTURAL PATTERNS
        // ------------------------------------------------------------------------
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("2️⃣ STRUCTURAL DESIGN PATTERNS");
        System.out.println("------------------------------------------------------------------------");

        // Adapter
        AdapterPattern.LegacyPaymentGateway legacyGateway = new AdapterPattern.LegacyPaymentGateway();
        AdapterPattern.PaymentProcessor adapter = new AdapterPattern.LegacyPaymentAdapter(legacyGateway);
        System.out.println("   [Adapter] " + adapter.processPayment("ACC-9874", 249.99));

        // Decorator
        DecoratorPattern.MemoryDataStream baseStream = new DecoratorPattern.MemoryDataStream();
        DecoratorPattern.MetricLoggingDecorator loggingStream = new DecoratorPattern.MetricLoggingDecorator(baseStream);
        DecoratorPattern.Base64EncodingDecorator decoratedStream = new DecoratorPattern.Base64EncodingDecorator(loggingStream);

        decoratedStream.write("CONFIDENTIAL_SRE_KEY_9923");
        System.out.println("   [Decorator Encoded Raw Storage] " + baseStream.read());
        System.out.println("   [Decorator Decoded Output]      " + decoratedStream.read());
        System.out.printf("   [Decorator Metrics] Written: %d bytes across %d writes\n",
                loggingStream.getTotalBytesWritten(), loggingStream.getWriteCount());

        // Proxy (JDK Dynamic Proxy java.lang.reflect.Proxy)
        ProxyPattern.OrderService realService = new ProxyPattern.RealOrderService();
        ProxyPattern.AuditAndLatencyInvocationHandler handler = new ProxyPattern.AuditAndLatencyInvocationHandler(realService);
        ProxyPattern.OrderService proxyService = ProxyPattern.createProxy(realService, handler);

        System.out.println("   [Proxy Target Output] " + proxyService.processOrder("ORD-1001", 150.00));
        System.out.println("   [Proxy Target Output] " + proxyService.cancelOrder("ORD-1001"));

        // Facade
        FacadePattern.OrderCheckoutFacade checkoutFacade = new FacadePattern.OrderCheckoutFacade(
                new FacadePattern.InventoryService(),
                new FacadePattern.PaymentGateway(),
                new FacadePattern.ShippingService(),
                new FacadePattern.EmailNotificationService()
        );
        System.out.println("   [Facade] " + checkoutFacade.placeOrder("CUST-404", "ITEM-99", 2, 49.95, "742 Evergreen Terrace"));

        // Composite
        CompositePattern.DirectoryNode rootDir = new CompositePattern.DirectoryNode("root");
        CompositePattern.DirectoryNode logDir = new CompositePattern.DirectoryNode("var_log");
        logDir.addNode(new CompositePattern.FileNode("syslog", 102400));
        logDir.addNode(new CompositePattern.FileNode("application.log", 204800));
        rootDir.addNode(logDir);
        rootDir.addNode(new CompositePattern.FileNode("config.yaml", 512));
        System.out.println("   [Composite Tree Hierarchy]");
        rootDir.printTree("   ");

        // ------------------------------------------------------------------------
        // 3. BEHAVIORAL PATTERNS
        // ------------------------------------------------------------------------
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("3️⃣ BEHAVIORAL DESIGN PATTERNS");
        System.out.println("------------------------------------------------------------------------");

        // Strategy
        StrategyPattern.ShoppingCart cart = new StrategyPattern.ShoppingCart();
        cart.setPaymentStrategy(new StrategyPattern.CreditCardStrategy("4111222233334444"));
        System.out.println("   [Strategy] " + cart.checkout(120.50));
        cart.setPaymentStrategy(new StrategyPattern.CryptoStrategy("0x71C7656EC7ab88b098defB751B7401B5f6d8976F"));
        System.out.println("   [Strategy Swapped] " + cart.checkout(120.50));

        // Observer
        ObserverPattern.MetricsPublisher publisher = new ObserverPattern.MetricsPublisher();
        ObserverPattern.AlertingEngineObserver alertObs = new ObserverPattern.AlertingEngineObserver(80.0);
        ObserverPattern.GrafanaExporterObserver grafanaObs = new ObserverPattern.GrafanaExporterObserver();

        publisher.subscribe(alertObs);
        publisher.subscribe(grafanaObs);
        System.out.println("   [Observer] Publishing metric: cpu_utilization = 45.0");
        publisher.publishMetric("cpu_utilization", 45.0);
        System.out.println("   [Observer] Publishing metric: cpu_utilization = 95.5 (Triggers Alert)");
        publisher.publishMetric("cpu_utilization", 95.5);

        // Command
        CommandPattern.ServerInstance serverNode = new CommandPattern.ServerInstance("k8s-node-01");
        CommandPattern.ControlPlaneInvoker invoker = new CommandPattern.ControlPlaneInvoker();

        System.out.println("   [Command] " + invoker.executeCommand(new CommandPattern.StartServerCommand(serverNode)));
        System.out.println("   [Command] " + invoker.executeCommand(new CommandPattern.ScaleClusterCommand(serverNode, 5)));
        System.out.println("   [Command Undo] " + invoker.undoLastCommand());

        // Template Method
        TemplateMethodPattern.DataIngestionPipeline jsonPipeline = new TemplateMethodPattern.JsonDataPipeline();
        System.out.println("   [Template Method JSON] " + jsonPipeline.runPipeline("s3://logs/2026-08-26.json"));

        // State
        StatePattern.OrderContext orderCtx = new StatePattern.OrderContext("ORD-9901");
        System.out.println("   [State Step 1] " + orderCtx.proceedNext()); // Created -> Paid
        System.out.println("   [State Step 2] " + orderCtx.proceedNext()); // Paid -> Shipped
        System.out.println("   [State Invalid Action] " + orderCtx.cancel()); // Cannot cancel shipped order

        // Chain of Responsibility
        ChainOfResponsibilityPattern.RequestHandler rateLimiter = new ChainOfResponsibilityPattern.RateLimitingHandler(100);
        ChainOfResponsibilityPattern.RequestHandler authenticator = new ChainOfResponsibilityPattern.AuthenticationHandler();
        ChainOfResponsibilityPattern.RequestHandler sanitizer = new ChainOfResponsibilityPattern.SanitizationHandler();

        rateLimiter.setNext(authenticator).setNext(sanitizer);

        ChainOfResponsibilityPattern.HttpRequest validReq = new ChainOfResponsibilityPattern.HttpRequest(
                "/api/v1/resource", "Bearer valid-token-123", "{\"query\":\"select\"}", 50);
        ChainOfResponsibilityPattern.HttpRequest maliciousReq = new ChainOfResponsibilityPattern.HttpRequest(
                "/api/v1/resource", "Bearer valid-token-123", "<script>alert(1)</script>", 50);

        System.out.printf("   [Chain Valid Request Result]: %b\n", rateLimiter.handle(validReq));
        System.out.printf("   [Chain Malicious Request Result]: %b\n", rateLimiter.handle(maliciousReq));

        long totalElapsed = System.currentTimeMillis() - startOverall;

        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("💡 SENIOR SRE DESIGN PATTERNS ARCHITECTURAL INSIGHTS");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("   1. Enum Singletons prevent reflection attacks & serialization leaks in high-throughput JVM apps.");
        System.out.println("   2. Dynamic Proxies (java.lang.reflect.Proxy) are the foundation of Spring AOP, Transactional & Security annotations.");
        System.out.println("   3. Always prefer Composition (Decorator / Strategy) over deep Inheritance hierarchies to prevent tight coupling.");
        System.out.println("   4. Builders produce unmodifiable/immutable domain state, eliminating concurrent modification bugs.");
        System.out.printf("======================================================================== [Completed in %d ms]\n", totalElapsed);
    }
}
