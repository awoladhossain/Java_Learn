package com.example.advanced;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Section 3.4.4: Reflection API & Custom Annotations.
 * 
 * Demonstrates:
 * - Custom Annotations (@Retention, @Target, default attributes).
 * - Reflection API (Class, Field, Method, Constructor).
 * - Inspecting private fields (setAccessible), invoking methods dynamically.
 * - Mini Framework Engine: Custom Dependency Injector & Test/Initializer Runner.
 */
public class ReflectionAndAnnotationsDemo {

    // =========================================================================
    // 1. Custom Annotation Definitions
    // =========================================================================

    /**
     * Class-level annotation designating managed component services.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ManagedService {
        String name();
        int version() default 1;
    }

    /**
     * Field-level annotation for automatic configuration injection.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InjectConfig {
        String key();
        String defaultValue() default "";
    }

    /**
     * Method-level annotation designating lifecycle initialization.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PostConstruct {
        int order() default 1;
    }

    /**
     * Method-level annotation designating audited execution.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AuditLog {
        String action();
    }

    // =========================================================================
    // 2. Annotated Domain Class
    // =========================================================================

    @ManagedService(name = "DatabaseConnectionPool", version = 2)
    public static class DatabaseService {

        @InjectConfig(key = "db.url", defaultValue = "jdbc:postgresql://localhost:5432/prod_db")
        private String dbUrl;

        @InjectConfig(key = "db.maxConnections", defaultValue = "50")
        private int maxConnections;

        private boolean initialized = false;

        public DatabaseService() {
            // Default zero-arg constructor for reflection instantiation
        }

        @PostConstruct(order = 1)
        public void initializePool() {
            this.initialized = true;
            System.out.println("  [Lifecycle] Pool Initialized -> URL: " + dbUrl + " | Max Conn: " + maxConnections);
        }

        @AuditLog(action = "EXECUTE_SQL")
        private String executeQuery(String sql) {
            if (!initialized) {
                throw new IllegalStateException("DatabaseService is not initialized!");
            }
            return "ResultSet for query [" + sql + "] executed against " + dbUrl;
        }

        public String getDbUrl() { return dbUrl; }
        public int getMaxConnections() { return maxConnections; }
        public boolean isInitialized() { return initialized; }
    }

    // =========================================================================
    // 3. Mini Reflection Engine / Framework Processor
    // =========================================================================

    public static class MiniFrameworkEngine {

        /**
         * Dynamically instantiates a class, injects annotated fields, and triggers @PostConstruct methods.
         */
        public static <T> T containerBootstrap(Class<T> clazz, Map<String, String> configSource) throws Exception {
            // 1. Verify Class Annotation @ManagedService
            if (!clazz.isAnnotationPresent(ManagedService.class)) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " is missing @ManagedService annotation!");
            }
            ManagedService serviceAnn = clazz.getAnnotation(ManagedService.class);
            System.out.println("🚀 Bootstrapping Service: " + serviceAnn.name() + " (v" + serviceAnn.version() + ")");

            // 2. Instantiate via Reflection (Constructor)
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();

            // 3. Process Field Annotations @InjectConfig
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectConfig.class)) {
                    InjectConfig configAnn = field.getAnnotation(InjectConfig.class);
                    String configKey = configAnn.key();
                    String rawValue = configSource.getOrDefault(configKey, configAnn.defaultValue());

                    field.setAccessible(true); // Bypass private access restriction

                    // Perform primitive / String conversion
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.setInt(instance, Integer.parseInt(rawValue));
                    } else if (field.getType() == String.class) {
                        field.set(instance, rawValue);
                    }
                    System.out.println("  [Injection] Injected " + field.getName() + " = " + rawValue + " (from key: " + configKey + ")");
                }
            }

            // 4. Process Method Annotations @PostConstruct
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    method.setAccessible(true);
                    method.invoke(instance); // Dynamically invoke method
                }
            }

            return instance;
        }

        /**
         * Dynamic Method Invocation with @AuditLog Interception.
         */
        public static Object invokeWithAuditing(Object instance, String methodName, Object... args) throws Exception {
            Class<?> clazz = instance.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    method.setAccessible(true);
                    if (method.isAnnotationPresent(AuditLog.class)) {
                        AuditLog audit = method.getAnnotation(AuditLog.class);
                        System.out.println("  [Audit Interceptor] 📝 Action: " + audit.action() + " | Method: " + method.getName());
                    }
                    return method.invoke(instance, args);
                }
            }
            throw new NoSuchMethodException("Method " + methodName + " not found on class " + clazz.getName());
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.4.4 REFLECTION API & CUSTOM ANNOTATIONS");
        System.out.println("------------------------------------------------------------------------");

        try {
            // 1. Config environment simulator
            Map<String, String> envConfig = new HashMap<>();
            envConfig.put("db.url", "jdbc:postgresql://db-cluster-primary.internal:5432/finance_db");
            envConfig.put("db.maxConnections", "100");

            // 2. Mini Framework Bootstrap using Reflection
            System.out.println("\n--- 1. Container Bootstrap via Reflection & Field Injection ---");
            DatabaseService dbService = MiniFrameworkEngine.containerBootstrap(DatabaseService.class, envConfig);

            System.out.println("\n--- 2. Validating Injected Service State ---");
            System.out.println("Service URL            : " + dbService.getDbUrl());
            System.out.println("Service Max Connections: " + dbService.getMaxConnections());
            System.out.println("Service Initialized?   : " + dbService.isInitialized());

            // 3. Dynamic Private Method Invocation & Audit Interception
            System.out.println("\n--- 3. Dynamic Private Method Invocation & Audit Log ---");
            Object queryResult = MiniFrameworkEngine.invokeWithAuditing(dbService, "executeQuery", "SELECT * FROM users WHERE active = true");
            System.out.println("Query Execution Output : " + queryResult);

            // 4. Detailed Reflection Class Inspection
            System.out.println("\n--- 4. Reflection Class Inspection Metrics ---");
            Class<?> clazz = DatabaseService.class;
            System.out.println("Class Name             : " + clazz.getName());
            System.out.println("Simple Name            : " + clazz.getSimpleName());
            System.out.println("Declared Fields Count  : " + clazz.getDeclaredFields().length);
            System.out.println("Declared Methods Count : " + clazz.getDeclaredMethods().length);

        } catch (Exception e) {
            System.err.println("Reflection Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n💡 SRE Framework Insight: Reflection powers Spring Boot (@Component, @Autowired),");
        System.out.println("   Jackson JSON serializers, and JUnit. While extremely powerful, reflection bypasses compile checks");
        System.out.println("   and incurs a small runtime overhead due to dynamic field/method lookups.");
    }
}
