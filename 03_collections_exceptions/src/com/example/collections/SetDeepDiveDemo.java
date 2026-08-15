package com.example.collections;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 3.2.2 Set Collections Deep-Dive
 * 
 * SRE & Internals Breakdown:
 * 1. HashSet:
 *    - Backed by an internal HashMap<E, Object> instance.
 *    - Elements added are keys in the HashMap; the value is a static dummy Object reference (PRESENT).
 *    - Unordered; relies strictly on element hashCode() and equals().
 * 
 * 2. LinkedHashSet:
 *    - Backed by a LinkedHashMap<E, Object>.
 *    - Maintains a doubly-linked list across hash buckets to preserve insertion order.
 *    - Slight memory overhead over HashSet due to before/after pointers on entry nodes.
 * 
 * 3. TreeSet:
 *    - Backed by a TreeMap<E, Object> (Red-Black Tree self-balancing binary search tree).
 *    - Operations (add, remove, contains) run in O(log n) time.
 *    - Key uniqueness is determined by compareTo() (Comparable) or compare() (Comparator), NOT equals()!
 */
public class SetDeepDiveDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.2.2 SET COLLECTIONS: HashSet vs LinkedHashSet vs TreeSet");
        System.out.println("------------------------------------------------------------------------");

        demonstrateHashSetHashMapBacking();
        demonstrateSetOrderDifferences();
        demonstrateTreeSetComparableVsComparator();
    }

    private static void demonstrateHashSetHashMapBacking() {
        System.out.println("\n--- 1. HashSet Underlying HashMap Mechanics ---");
        Set<String> hashSet = new HashSet<>();
        hashSet.add("Node-Alpha");
        hashSet.add("Node-Beta");
        hashSet.add("Node-Gamma");

        try {
            Field mapField = HashSet.class.getDeclaredField("map");
            mapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> backingMap = (Map<String, Object>) mapField.get(hashSet);

            System.out.println("HashSet elements count: " + hashSet.size());
            System.out.println("Backing HashMap class : " + backingMap.getClass().getName());
            System.out.println("Backing HashMap keys  : " + backingMap.keySet());
            System.out.println("Backing HashMap values: " + backingMap.values());

            // Inspect the PRESENT dummy object
            Field presentField = HashSet.class.getDeclaredField("PRESENT");
            presentField.setAccessible(true);
            Object dummyObj = presentField.get(null);
            System.out.println("Static dummy Object (PRESENT) hash: " + System.identityHashCode(dummyObj));
        } catch (Exception e) {
            System.err.println("Reflection error inspecting HashSet: " + e.getMessage());
        }

        System.out.println("💡 SRE Takeaway: HashSet contains NO independent hashing logic; it delegates completely to HashMap.");
    }

    private static void demonstrateSetOrderDifferences() {
        System.out.println("\n--- 2. Set Ordering: HashSet vs LinkedHashSet vs TreeSet ---");
        String[] data = {"Kubernetes", "Docker", "Ansible", "Terraform", "Prometheus"};

        Set<String> hashSet = new HashSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        Set<String> treeSet = new TreeSet<>();

        for (String item : data) {
            hashSet.add(item);
            linkedHashSet.add(item);
            treeSet.add(item);
        }

        System.out.println("Input Order    : [Kubernetes, Docker, Ansible, Terraform, Prometheus]");
        System.out.println("HashSet Order  : " + hashSet + " (Hash bucket placement - Unordered)");
        System.out.println("LinkedHashSet  : " + linkedHashSet + " (Doubly linked list - Preserves Insertion Order)");
        System.out.println("TreeSet Order  : " + treeSet + " (Red-Black tree - Alphabetically Sorted)");
    }

    private static void demonstrateTreeSetComparableVsComparator() {
        System.out.println("\n--- 3. TreeSet: Comparable vs Comparator & Equality Contract ---");

        // Custom Domain Object with Comparable (Natural sort by Server ID)
        Set<ServerInfo> naturalTreeSet = new TreeSet<>();
        naturalTreeSet.add(new ServerInfo(103, "web-srv-3", 16));
        naturalTreeSet.add(new ServerInfo(101, "web-srv-1", 64));
        naturalTreeSet.add(new ServerInfo(102, "web-srv-2", 32));

        System.out.println("Natural Ordering (Comparable by ID):");
        for (ServerInfo server : naturalTreeSet) {
            System.out.println("  " + server);
        }

        // Custom Comparator (Sorted by RAM Descending)
        Comparator<ServerInfo> ramDescComparator = Comparator.comparingInt(ServerInfo::ramGb).reversed()
                .thenComparingInt(ServerInfo::id);

        Set<ServerInfo> customTreeSet = new TreeSet<>(ramDescComparator);
        customTreeSet.addAll(naturalTreeSet);

        System.out.println("\nCustom Comparator (Sorted by RAM Descending):");
        for (ServerInfo server : customTreeSet) {
            System.out.println("  " + server);
        }

        // TreeSet uniqueness hazard: compareTo returning 0 treats objects as identical!
        Set<ServerInfo> flawedTreeSet = new TreeSet<>(Comparator.comparing(ServerInfo::ramGb)); // Only compares RAM
        flawedTreeSet.add(new ServerInfo(201, "srv-a", 32));
        flawedTreeSet.add(new ServerInfo(202, "srv-b", 32)); // Duplicate RAM!

        System.out.println("\n⚠️ TreeSet Duplicate Hazard (Comparator compares ONLY RAM):");
        System.out.println("  Added 2 servers with 32GB RAM. Total elements in TreeSet: " + flawedTreeSet.size());
        System.out.println("💡 SRE Rule: TreeSet/TreeMap Comparator MUST be consistent with equals() (or tie-break via secondary fields like ID)");
        System.out.println("   otherwise elements with identical sort keys will be silently discarded!");
    }

    // Record for clean domain model representation
    private record ServerInfo(int id, String hostname, int ramGb) implements Comparable<ServerInfo> {
        @Override
        public int compareTo(ServerInfo other) {
            return Integer.compare(this.id, other.id);
        }
    }
}
