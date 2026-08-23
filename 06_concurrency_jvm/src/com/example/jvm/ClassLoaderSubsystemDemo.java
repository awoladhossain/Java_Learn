package com.example.jvm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Section 6.2.1: Class Loader Subsystem.
 * 
 * Demonstrates:
 * - 1. Parent Delegation Model & ClassLoader Hierarchy (Bootstrap, Platform, Application).
 * - 2. Class Loading Lifecycle: Loading, Linking (Verification, Preparation, Resolution), Initialization.
 * - 3. Custom ClassLoader implementation demonstrating dynamic bytecode loading & namespace isolation.
 * - 4. SRE Insights: Memory leaks caused by custom ClassLoader references (Metaspace retention).
 */
public class ClassLoaderSubsystemDemo {

    /**
     * Helper class to demonstrate static variable preparation vs initialization.
     */
    public static class LifecycleTarget {
        // Static variable initialized during Initialization phase (<clinit>)
        public static int initializedCounter = 100;
        
        // Static variable exhibiting preparation phase default (0) before initialization
        public static final String CONSTANT_VAL = "STATIC_FINAL_CONSTANT";

        static {
            System.out.println("   [INITIALIZATION] Static block execution (<clinit>) in LifecycleTarget.");
            System.out.println("   [INITIALIZATION] Counter initialized to: " + initializedCounter);
        }

        public LifecycleTarget() {
            System.out.println("   [INSTANTIATION] Instance constructor (<init>) executed.");
        }
    }

    /**
     * Custom ClassLoader that loads raw class bytes dynamically to demonstrate
     * isolated loading and bypassing/verifying parent delegation.
     */
    public static class CustomMemoryClassLoader extends ClassLoader {
        
        private final String loaderName;

        public CustomMemoryClassLoader(String loaderName, ClassLoader parent) {
            super(parent);
            this.loaderName = loaderName;
        }

        @Override
        public String toString() {
            return "CustomMemoryClassLoader{" + loaderName + "}";
        }

        /**
         * Loads bytecode of a target class from classloader resource stream.
         */
        public Class<?> loadClassFromBytes(String className) throws ClassNotFoundException {
            try {
                String resourcePath = className.replace('.', '/') + ".class";
                InputStream is = getParent().getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new ClassNotFoundException("Could not find class bytes for: " + className);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] rawBytes = baos.toByteArray();

                // Define class in this ClassLoader's namespace
                return defineClass(className, rawBytes, 0, rawBytes.length);
            } catch (Exception e) {
                throw new ClassNotFoundException("Failed to define class " + className, e);
            }
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 6.2.1 CLASS LOADER SUBSYSTEM & PARENT DELEGATION MODEL");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Parent Delegation Model & ClassLoader Hierarchy
        // ==========================================
        System.out.println("\n--- 1. ClassLoader Hierarchy & Parent Delegation ---");

        // Bootstrap ClassLoader loads java.lang.Object (Returns null in JVM API)
        ClassLoader bootstrapLoader = String.class.getClassLoader();
        System.out.println("   [1] String.class Loader (Bootstrap Loader)  : " + (bootstrapLoader == null ? "null (Bootstrap ClassLoader written in C/C++)" : bootstrapLoader));

        // Platform / Extension ClassLoader loads java.sql.DriverManager
        ClassLoader platformLoader = java.sql.DriverManager.class.getClassLoader();
        System.out.println("   [2] DriverManager Loader (Platform Loader)   : " + platformLoader);

        // Application / System ClassLoader loads application classes
        ClassLoader appLoader = ClassLoaderSubsystemDemo.class.getClassLoader();
        System.out.println("   [3] Application Class Loader (AppLoader)     : " + appLoader);
        System.out.println("   [4] AppLoader Parent                         : " + appLoader.getParent());
        System.out.println("   [5] AppLoader Grandparent (Platform Parent)  : " + appLoader.getParent().getParent());

        // ==========================================
        // 2. Class Loading Lifecycle Phases
        // ==========================================
        System.out.println("\n--- 2. Class Lifecycle: Loading, Linking & Initialization ---");
        System.out.println("   [LOADING] Fetching binary representation of class...");
        System.out.println("   [LINKING - Verification] Ensuring bytecode complies with JVM Spec (0xCAFEBABE magic header).");
        System.out.println("   [LINKING - Preparation] Allocating static memory & setting default values (e.g. primitive int -> 0).");
        System.out.println("   [LINKING - Resolution] Translating symbolic references to direct memory offsets.");

        try {
            System.out.println("   [TRIGGER] Referencing static constant (No class initialization triggered):");
            String constVal = LifecycleTarget.CONSTANT_VAL;
            System.out.println("   [RESULT] Constant value fetched: " + constVal);

            System.out.println("\n   [TRIGGER] Explicit Class.forName() forcing initialization (<clinit>):");
            Class<?> clazz = Class.forName("com.example.jvm.ClassLoaderSubsystemDemo$LifecycleTarget");
            System.out.println("   [RESULT] Class initialized successfully: " + clazz.getName());

            System.out.println("\n   [TRIGGER] Creating new instance (<init>):");
            LifecycleTarget instance = new LifecycleTarget();
            System.out.println("   [RESULT] Instance created: " + instance);

        } catch (ClassNotFoundException e) {
            System.err.println("Class loading failed: " + e.getMessage());
        }

        // ==========================================
        // 3. Custom ClassLoader & Namespace Isolation
        // ==========================================
        System.out.println("\n--- 3. Custom ClassLoader & Namespace Isolation ---");

        try {
            CustomMemoryClassLoader customLoader1 = new CustomMemoryClassLoader("Loader-Alpha", appLoader);
            CustomMemoryClassLoader customLoader2 = new CustomMemoryClassLoader("Loader-Beta", appLoader);

            System.out.println("   [DELEGATION] Standard loadClass() delegates up to parent: " + customLoader1);
            Class<?> standardClass = customLoader1.loadClass("com.example.jvm.ClassLoaderSubsystemDemo$LifecycleTarget");
            System.out.println("   Class loaded via standard delegation: " + standardClass.hashCode() + " [Loader: " + standardClass.getClassLoader() + "]");

            System.out.println("\n   [ISOLATION] Loading identical class in two independent custom loaders:");
            Class<?> classFromLoader1 = customLoader1.loadClassFromBytes("com.example.jvm.ClassLoaderSubsystemDemo$LifecycleTarget");
            Class<?> classFromLoader2 = customLoader2.loadClassFromBytes("com.example.jvm.ClassLoaderSubsystemDemo$LifecycleTarget");

            System.out.println("   Class 1 Hash: " + classFromLoader1.hashCode() + " | Loader: " + classFromLoader1.getClassLoader());
            System.out.println("   Class 2 Hash: " + classFromLoader2.hashCode() + " | Loader: " + classFromLoader2.getClassLoader());
            System.out.println("   Are Class 1 and Class 2 equal? " + (classFromLoader1 == classFromLoader2));
            System.out.println("   Is Class 1 instance of Class 2 type? " + classFromLoader1.isInstance(classFromLoader2.getDeclaredConstructor().newInstance()));

            System.out.println("   💡 SRE Insight: Two classes with the exact same binary bytecode loaded by different ClassLoaders are distinct types in the JVM! This enables plugin architecture hot-reloading but can cause ClassCastException if instances cross loader boundaries.");

        } catch (Exception e) {
            System.err.println("Custom classloader demonstration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
