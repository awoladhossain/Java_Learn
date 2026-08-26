package com.example.patterns;

import com.example.patterns.creational.*;
import com.example.patterns.structural.*;
import com.example.patterns.behavioral.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 7.2: Software Design Patterns (GoF) Test Suite")
public class DesignPatternsTest {

    @Nested
    @DisplayName("Creational Patterns Tests")
    class CreationalPatternsTest {

        @Test
        @DisplayName("Singleton: Double-Checked Locking thread safety & instance uniqueness")
        void testDoubleCheckedLockingSingleton() throws Exception {
            int threadCount = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(1);
            SingletonPattern.DoubleCheckedLockingSingleton[] instances = new SingletonPattern.DoubleCheckedLockingSingleton[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        latch.await();
                        instances[index] = SingletonPattern.DoubleCheckedLockingSingleton.getInstance();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            latch.countDown();
            executor.shutdown();
            boolean finished = executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            assertTrue(finished, "Thread pool tasks should complete within timeout");

            // Verify all threads received the exact same instance reference
            SingletonPattern.DoubleCheckedLockingSingleton firstInstance = SingletonPattern.DoubleCheckedLockingSingleton.getInstance();
            assertNotNull(firstInstance);
            for (SingletonPattern.DoubleCheckedLockingSingleton inst : instances) {
                assertSame(firstInstance, inst, "All threads must reference the identical Singleton instance");
            }
        }

        @Test
        @DisplayName("Singleton: Reflection protection throws exception on private constructor call")
        void testSingletonReflectionProtection() {
            SingletonPattern.DoubleCheckedLockingSingleton instance = SingletonPattern.DoubleCheckedLockingSingleton.getInstance();
            assertNotNull(instance);

            Constructor<?>[] constructors = SingletonPattern.DoubleCheckedLockingSingleton.class.getDeclaredConstructors();
            assertEquals(1, constructors.length);

            Constructor<?> constructor = constructors[0];
            constructor.setAccessible(true);

            InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals("Instance already created! Use getInstance().", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Singleton: Enum Singleton uniqueness and functionality")
        void testEnumSingleton() {
            SingletonPattern.EnumSingleton inst1 = SingletonPattern.EnumSingleton.INSTANCE;
            SingletonPattern.EnumSingleton inst2 = SingletonPattern.EnumSingleton.INSTANCE;
            assertSame(inst1, inst2);
            assertNotNull(inst1.getConnectionPoolId());
        }

        @Test
        @DisplayName("Factory Method: Polymorphic notification creation")
        void testFactoryMethod() {
            FactoryMethodPattern.NotificationFactory emailFactory = new FactoryMethodPattern.EmailNotificationFactory();
            FactoryMethodPattern.NotificationFactory smsFactory = new FactoryMethodPattern.SmsNotificationFactory();

            String emailRes = emailFactory.notifyUser("user@domain.com", "Alert 1");
            String smsRes = smsFactory.notifyUser("+15550199", "Alert 2");

            assertTrue(emailRes.contains("[EMAIL]"));
            assertTrue(smsRes.contains("[SMS]"));
        }

        @Test
        @DisplayName("Abstract Factory: Creates matching families of objects")
        void testAbstractFactory() {
            AbstractFactoryPattern.CloudResourceFactory awsFactory = new AbstractFactoryPattern.AwsResourceFactory();
            AbstractFactoryPattern.ComputeInstance awsVm = awsFactory.createComputeInstance();
            AbstractFactoryPattern.StorageBucket awsBucket = awsFactory.createStorageBucket("test-bucket");

            assertTrue(awsVm.launch().contains("AWS EC2"));
            assertTrue(awsBucket.storeFile("doc.pdf").contains("s3://test-bucket/"));
        }

        @Test
        @DisplayName("Builder: Step-by-step construction & validation")
        void testBuilderPattern() {
            BuilderPattern.HttpRequest request = new BuilderPattern.HttpRequest.Builder("https://api.example.com/v1/resource")
                    .method("POST")
                    .header("Accept", "application/json")
                    .connectTimeoutMs(3000)
                    .build();

            assertEquals("POST", request.getMethod());
            assertEquals("https://api.example.com/v1/resource", request.getUrl());
            assertEquals("application/json", request.getHeaders().get("Accept"));
            assertEquals(3000, request.getConnectTimeoutMs());

            assertThrows(IllegalArgumentException.class, () -> new BuilderPattern.HttpRequest.Builder(""));
        }

        @Test
        @DisplayName("Prototype: Deep cloning state isolation")
        void testPrototypePattern() {
            PrototypePattern.ServerConfiguration prototype = new PrototypePattern.ServerConfiguration("prod", 100, Map.of("v2", "true"));
            PrototypePattern.ServerConfiguration clone = prototype.clonePrototype();

            assertNotSame(prototype, clone);
            assertEquals(prototype.getEnvironment(), clone.getEnvironment());

            // Modify clone feature flags
            clone.setFeatureFlag("v2", "false");
            assertEquals("true", prototype.getFeatureFlags().get("v2"), "Prototype map must remain unaffected");
            assertEquals("false", clone.getFeatureFlags().get("v2"));
        }
    }

    @Nested
    @DisplayName("Structural Patterns Tests")
    class StructuralPatternsTest {

        @Test
        @DisplayName("Adapter: Converts legacy interface to modern target interface")
        void testAdapterPattern() {
            AdapterPattern.LegacyPaymentGateway legacy = new AdapterPattern.LegacyPaymentGateway();
            AdapterPattern.PaymentProcessor adapter = new AdapterPattern.LegacyPaymentAdapter(legacy);

            String result = adapter.processPayment("ACC-1234", 99.99);
            assertTrue(result.contains("SUCCESS: Legacy payment processed"));
        }

        @Test
        @DisplayName("Decorator: Layered data stream processing & metric logging")
        void testDecoratorPattern() {
            DecoratorPattern.MemoryDataStream base = new DecoratorPattern.MemoryDataStream();
            DecoratorPattern.MetricLoggingDecorator logging = new DecoratorPattern.MetricLoggingDecorator(base);
            DecoratorPattern.Base64EncodingDecorator base64 = new DecoratorPattern.Base64EncodingDecorator(logging);

            base64.write("HELLO_WORLD");
            assertEquals("HELLO_WORLD", base64.read());
            assertEquals(1, logging.getWriteCount());
            assertEquals(1, logging.getReadCount());
        }

        @Test
        @DisplayName("Proxy: Dynamic Proxy (java.lang.reflect.Proxy) method invocation & audit tracking")
        void testDynamicProxyPattern() {
            ProxyPattern.OrderService realService = new ProxyPattern.RealOrderService();
            ProxyPattern.AuditAndLatencyInvocationHandler handler = new ProxyPattern.AuditAndLatencyInvocationHandler(realService);
            ProxyPattern.OrderService proxy = ProxyPattern.createProxy(realService, handler);

            String res1 = proxy.processOrder("ORD-001", 100.0);
            String res2 = proxy.cancelOrder("ORD-001");

            assertTrue(res1.contains("processed successfully"));
            assertTrue(res2.contains("cancelled successfully"));
            assertEquals(2, handler.getInvocationCount());
        }

        @Test
        @DisplayName("Facade: Unified order checkout subsystem orchestration")
        void testFacadePattern() {
            FacadePattern.OrderCheckoutFacade facade = new FacadePattern.OrderCheckoutFacade(
                    new FacadePattern.InventoryService(),
                    new FacadePattern.PaymentGateway(),
                    new FacadePattern.ShippingService(),
                    new FacadePattern.EmailNotificationService()
            );

            String result = facade.placeOrder("CUST-1", "ITEM-10", 1, 50.0, "123 Main St");
            assertTrue(result.contains("ORDER SUCCESSFUL"));
        }

        @Test
        @DisplayName("Composite: Recursive tree total size calculation")
        void testCompositePattern() {
            CompositePattern.DirectoryNode dir = new CompositePattern.DirectoryNode("logs");
            dir.addNode(new CompositePattern.FileNode("app.log", 1000));
            dir.addNode(new CompositePattern.FileNode("err.log", 2000));

            assertEquals(3000, dir.getSize());
        }
    }

    @Nested
    @DisplayName("Behavioral Patterns Tests")
    class BehavioralPatternsTest {

        @Test
        @DisplayName("Strategy: Dynamic strategy substitution")
        void testStrategyPattern() {
            StrategyPattern.ShoppingCart cart = new StrategyPattern.ShoppingCart();
            cart.setPaymentStrategy(new StrategyPattern.PayPalStrategy("user@pay.com"));
            String res1 = cart.checkout(50.0);

            cart.setPaymentStrategy(new StrategyPattern.CreditCardStrategy("1234567890123456"));
            String res2 = cart.checkout(100.0);

            assertTrue(res1.contains("via PayPal"));
            assertTrue(res2.contains("ending in 3456"));
        }

        @Test
        @DisplayName("Observer: Pub/Sub metric notification trigger")
        void testObserverPattern() {
            ObserverPattern.MetricsPublisher publisher = new ObserverPattern.MetricsPublisher();
            ObserverPattern.AlertingEngineObserver alertObs = new ObserverPattern.AlertingEngineObserver(50.0);
            publisher.subscribe(alertObs);

            publisher.publishMetric("cpu", 30.0);
            assertEquals(0, alertObs.getAlertCount());

            publisher.publishMetric("cpu", 85.0);
            assertEquals(1, alertObs.getAlertCount());
        }

        @Test
        @DisplayName("Command: Execution queue & undo history")
        void testCommandPattern() {
            CommandPattern.ServerInstance server = new CommandPattern.ServerInstance("srv-1");
            CommandPattern.ControlPlaneInvoker invoker = new CommandPattern.ControlPlaneInvoker();

            invoker.executeCommand(new CommandPattern.StartServerCommand(server));
            assertTrue(server.isRunning());

            invoker.executeCommand(new CommandPattern.ScaleClusterCommand(server, 4));
            assertEquals(4, server.getInstancesCount());

            invoker.undoLastCommand();
            assertEquals(1, server.getInstancesCount());
        }

        @Test
        @DisplayName("Template Method: Skeleton ETL flow execution")
        void testTemplateMethodPattern() {
            TemplateMethodPattern.DataIngestionPipeline jsonPipeline = new TemplateMethodPattern.JsonDataPipeline();
            String output = jsonPipeline.runPipeline("s3://data.json");

            assertTrue(output.contains("Extracted raw JSON"));
            assertTrue(output.contains("Persisted DTOs"));
        }

        @Test
        @DisplayName("State: Order status lifecycle state machine transitions")
        void testStatePattern() {
            StatePattern.OrderContext context = new StatePattern.OrderContext("ORD-55");
            assertEquals("CREATED", context.getState().getStatus());

            context.proceedNext();
            assertEquals("PAID", context.getState().getStatus());

            context.proceedNext();
            assertEquals("SHIPPED", context.getState().getStatus());

            String cancelRes = context.cancel();
            assertTrue(cancelRes.contains("ERROR: Cannot cancel"));
            assertEquals("SHIPPED", context.getState().getStatus());
        }

        @Test
        @DisplayName("Chain of Responsibility: Middleware chain processing & short-circuiting")
        void testChainOfResponsibilityPattern() {
            ChainOfResponsibilityPattern.RequestHandler rateLimiter = new ChainOfResponsibilityPattern.RateLimitingHandler(10);
            ChainOfResponsibilityPattern.RequestHandler auth = new ChainOfResponsibilityPattern.AuthenticationHandler();
            rateLimiter.setNext(auth);

            ChainOfResponsibilityPattern.HttpRequest okReq = new ChainOfResponsibilityPattern.HttpRequest("/api", "Bearer valid-123", "body", 5);
            ChainOfResponsibilityPattern.HttpRequest unauthReq = new ChainOfResponsibilityPattern.HttpRequest("/api", "invalid-token", "body", 5);
            ChainOfResponsibilityPattern.HttpRequest highRateReq = new ChainOfResponsibilityPattern.HttpRequest("/api", "Bearer valid-123", "body", 50);

            assertTrue(rateLimiter.handle(okReq));
            assertFalse(rateLimiter.handle(unauthReq));
            assertFalse(rateLimiter.handle(highRateReq));
        }
    }
}
