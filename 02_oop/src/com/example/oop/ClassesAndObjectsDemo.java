package com.example.oop;

/**
 * Phase 2.1: Classes, Objects & Constructors
 * 
 * Main runner demonstrating:
 * 1. State (fields) and Behavior (methods) encapsulation
 * 2. All Constructor Types: Default, Parameterized, Copy, and Private (Singleton Pattern)
 * 3. Keywords in Action: 'this', 'super', 'static' (Class vs Instance memory), and 'final'
 * 4. Senior SRE Insights on Object Lifecycle, Thread Safety, and Static Memory Leaks
 */
public class ClassesAndObjectsDemo {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 2.1: Classes, Objects & Constructors Deep Dive");
        System.out.println("========================================================================");

        demonstrateConstructorsAndState();
        demonstrateInheritanceAndSuperKeyword();
        demonstratePrivateConstructorAndSingleton();
        demonstrateStaticVsInstanceMemoryLayout();
        demonstrateFinalKeywordRules();
        demonstrateSreOopInsights();

        System.out.println("\n========================================================================");
        System.out.println("✅ Phase 2.1 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Constructor Types: Default, Parameterized, and Copy Constructors
     */
    private static void demonstrateConstructorsAndState() {
        System.out.println("\n1️⃣  CONSTRUCTOR TYPES & INSTANCE STATE (FIELDS & METHODS):");
        System.out.println("------------------------------------------------------------------------");

        // A. Default Constructor (Chained via this(...))
        ServerNode defaultNode = new ServerNode();
        System.out.println("   📍 Default Constructor Instance (Chained via this(...)):");
        System.out.println("      • " + defaultNode.getNodeSummary());

        // B. Parameterized Constructor
        ServerNode node1 = new ServerNode("node-us-east-1a", "10.0.1.15", 9090);
        node1.updateCpuUsage(42.8);
        System.out.println("\n   📍 Parameterized Constructor Instance:");
        System.out.println("      • " + node1.getNodeSummary());

        // C. Copy Constructor
        ServerNode node1Copy = new ServerNode(node1);
        System.out.println("\n   📍 Copy Constructor Instance (Cloned from node1):");
        System.out.println("      • Original IdentityHashCode : 0x" + Integer.toHexString(System.identityHashCode(node1)));
        System.out.println("      • Copy IdentityHashCode     : 0x" + Integer.toHexString(System.identityHashCode(node1Copy)));
        System.out.println("      • Copy Summary              : " + node1Copy.getNodeSummary());
    }

    /**
     * 2️⃣ Inheritance & 'super' Keyword Usage
     */
    private static void demonstrateInheritanceAndSuperKeyword() {
        System.out.println("\n2️⃣  INHERITANCE & 'super' KEYWORD MECHANICS:");
        System.out.println("------------------------------------------------------------------------");

        ProductionServerNode prodNode = new ProductionServerNode(
                "node-prod-01", "172.16.0.4", 8443, "production", "us-east-1a"
        );
        prodNode.updateCpuUsage(78.4);

        System.out.println("   📍 ProductionServerNode (subclass of ServerNode):");
        System.out.println("      • super(...) chained parent constructor successfully.");
        System.out.println("      • Polymorphic Summary (@Override using super.getNodeSummary()):");
        System.out.println("        " + prodNode.getNodeSummary());
    }

    /**
     * 3️⃣ Private Constructor & Singleton Pattern
     */
    private static void demonstratePrivateConstructorAndSingleton() {
        System.out.println("\n3️⃣  PRIVATE CONSTRUCTOR & SINGLETON PATTERN:");
        System.out.println("------------------------------------------------------------------------");

        // Attempting 'new ClusterConfigManager()' directly causes a compile error due to private constructor!
        ClusterConfigManager config1 = ClusterConfigManager.getInstance();
        ClusterConfigManager config2 = ClusterConfigManager.getInstance();

        System.out.println("   📍 Bill Pugh Thread-Safe Singleton Instances:");
        System.out.println("      • config1 IdentityHashCode : 0x" + Integer.toHexString(System.identityHashCode(config1)));
        System.out.println("      • config2 IdentityHashCode : 0x" + Integer.toHexString(System.identityHashCode(config2)));
        System.out.println("      • config1 == config2 ?       " + (config1 == config2) + " (Guaranteed Single Instance!)");

        System.out.println("\n   📍 Singleton State Access:");
        System.out.println("      • Cluster Name (static final) : " + ClusterConfigManager.CLUSTER_NAME);
        System.out.println("      • Max Connections (static)    : " + ClusterConfigManager.MAX_CONNECTIONS);
        System.out.println("      • Timeout Config              : " + config1.getConfig("timeout_ms") + " ms");
    }

    /**
     * 4️⃣ 'static' Memory Layout: Class Variables vs Instance Variables
     */
    private static void demonstrateStaticVsInstanceMemoryLayout() {
        System.out.println("\n4️⃣  'static' KEYWORD: CLASS VARIABLES vs INSTANCE VARIABLES:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   📍 Instance Fields vs Static Fields:");
        System.out.println("      • Instance Fields (nodeId, port, cpuUsage) reside inside each Object on Heap.");
        System.out.println("      • Static Field (totalNodeCount) resides in Class metadata (Metaspace/Heap) shared across all instances.");
        System.out.println("      • Total ServerNode Instances Created So Far: " + ServerNode.getTotalNodeCount());
    }

    /**
     * 5️⃣ 'final' Keyword Rules: Variables, Methods, and Classes
     */
    private static void demonstrateFinalKeywordRules() {
        System.out.println("\n5️⃣  'final' KEYWORD RULES & IMMUTABILITY BOUNDS:");
        System.out.println("------------------------------------------------------------------------");

        // A. Final Local Variable / Reference
        final ServerNode immutableRef = new ServerNode("node-final", "10.0.0.99", 8080);
        // immutableRef = new ServerNode(...); // COMPILE ERROR: Cannot reassign a final reference!
        
        // Note: 'final' reference prevents reassignment of pointer, but target object fields can mutate unless fields themselves are final!
        immutableRef.updateCpuUsage(15.0); // Allowed because fields are mutated inside the object

        System.out.println("   📍 Final Keyword Rules Summary:");
        System.out.println("      • final Variable : Value/Reference cannot be reassigned once initialized.");
        System.out.println("      • final Method   : Cannot be overridden in child classes (e.g., ServerNode.getNodeId()).");
        System.out.println("      • final Class    : Cannot be extended/inherited (e.g., ProductionServerNode).");
        System.out.println("      • Reference Target State : " + immutableRef.getNodeSummary());
    }

    /**
     * 6️⃣ Senior SRE OOP Insights & Architecture
     */
    private static void demonstrateSreOopInsights() {
        System.out.println("\n6️⃣  SENIOR SRE OOP & ARCHITECTURE INSIGHTS:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Static Memory Leak Risk in SRE Production Services:");
        System.out.println("      • Holding instance references inside 'static Collection' fields (e.g. static List<ServerNode>)");
        System.out.println("        prevents Garbage Collection indefinitely because the root GC thread retains static class references.");
        System.out.println("      • Always clear static collections or use WeakHashMap for temporary caching.");

        System.out.println("\n   💡 Thread-Safe Bill Pugh Singleton:");
        System.out.println("      • Traditional Lazy Singleton with 'synchronized getInstance()' incurs synchronized locking overhead on every read.");
        System.out.println("      • Bill Pugh Singleton leverages JVM ClassLoader guarantees for lazy, thread-safe initialization with ZERO sync overhead!");

        System.out.println("\n   💡 Invariant Protection via Private/Copy Constructors:");
        System.out.println("      • Use Copy Constructors or Defensive Copying in constructors to prevent callers");
        System.out.println("        from mutating internal state pointers (e.g. Date or List fields) outside domain invariants.");
    }
}
