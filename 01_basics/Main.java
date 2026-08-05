/**
 * Phase 1: Java Basics & Runtime Inspection
 * Senior SRE Perspective: Understanding JVM Runtime Environment
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("🚀 Welcome to Java Production & Masterclass!");
        System.out.println("==================================================");

        // 1. Basic System & Java Version Check
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        String osName = System.getProperty("os.name");

        System.out.println("📌 Environment Details:");
        System.out.println("   • Java Version : " + javaVersion);
        System.out.println("   • Java Vendor  : " + javaVendor);
        System.out.println("   • OS Name      : " + osName);

        // 2. JVM Memory Inspection (SRE Core Concept)
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
        long freeMemoryMB = runtime.freeMemory() / (1024 * 1024);

        System.out.println("\n🧠 JVM Memory Status (Heap):");
        System.out.println("   • Max Memory   : " + maxMemoryMB + " MB");
        System.out.println("   • Total Memory : " + totalMemoryMB + " MB");
        System.out.println("   • Free Memory  : " + freeMemoryMB + " MB");
        System.out.println("==================================================");
        System.out.println("✅ Setup complete! You are ready to start Phase 1.");
    }
}
