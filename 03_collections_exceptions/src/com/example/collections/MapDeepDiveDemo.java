package com.example.collections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 3.2.3 Map Collections Deep-Dive
 * 
 * SRE & Internals Breakdown:
 * 1. HashMap Mechanics:
 *    - Bucket Array: Node<K,V>[] table. Table length n is always a power of 2 (16, 32, 64...).
 *    - Bit-Spreading perturbation hash function: (h ^ (h >>> 16)) spreads higher bits to lower bits.
 *    - Bucket Index formula: index = (n - 1) & hash. Bitwise AND is significantly faster than modulo (hash % n).
 *    - hashCode() & equals() Contract: Equal objects MUST produce equal hashCodes. Violating this breaks lookups!
 *    - Load Factor & Threshold: Default loadFactor = 0.75. Rehashing doubles table size when size > threshold.
 *    - Treeification (Java 8+): When a bin count exceeds 8 AND table capacity >= 64, linked bin converts to Red-Black Tree (TreeNode).
 *      Changes worst-case lookup from O(n) hash collision attack to O(log n).
 *    - Untreeification: When bin count drops to <= 6 during resize, tree converts back to regular linked list.
 * 
 * 2. LinkedHashMap:
 *    - Extends HashMap. Entries have before/after pointers.
 *    - Access-Order mode (accessOrder=true) allows building an LRU (Least Recently Used) cache by overriding removeEldestEntry().
 * 
 * 3. TreeMap:
 *    - Implements NavigableMap based on Red-Black Tree.
 *    - Guarantees O(log n) key lookup/insertion/deletion and ordered iteration.
 */
public class MapDeepDiveDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.2.3 MAP COLLECTIONS: HashMap Internals, LinkedHashMap & TreeMap");
        System.out.println("------------------------------------------------------------------------");

        demonstrateHashMapInternalsAndPerturbation();
        demonstrateHashCodeEqualsContractViolation();
        demonstrateHashMapTreeification();
        demonstrateLinkedHashMapLruCache();
        demonstrateTreeMapNavigableOperations();
    }

    private static void demonstrateHashMapInternalsAndPerturbation() {
        System.out.println("\n--- 1. HashMap Internal Mechanics & Hash Bit-Spreading ---");
        HashMap<String, String> map = new HashMap<>(16, 0.75f);
        map.put("service.name", "payment-api");
        map.put("service.env", "production");

        System.out.println("Initial HashMap capacity: " + getHashMapCapacity(map));
        System.out.println("Threshold (16 * 0.75)   : " + (16 * 0.75f));

        // Demonstration of Bit-Spreading Perturbation Function: (h ^ (h >>> 16))
        String key = "database.url";
        int h = key.hashCode();
        int hash = h ^ (h >>> 16);
        int n = 16; // default capacity
        int bucketIndex = (n - 1) & hash;

        System.out.println("\nKey: '" + key + "'");
        System.out.println("  - Raw key.hashCode()       : " + Integer.toBinaryString(h) + " (Decimal: " + h + ")");
        System.out.println("  - XOR Shift (h >>> 16)     : " + Integer.toBinaryString(h >>> 16));
        System.out.println("  - Perturbed Hash (h^h>>>16): " + Integer.toBinaryString(hash));
        System.out.println("  - Table Mask (n - 1 = 15)  : " + Integer.toBinaryString(n - 1));
        System.out.println("  - Calculated Bucket Index  : " + bucketIndex + " [via (n - 1) & hash]");
    }

    private static void demonstrateHashCodeEqualsContractViolation() {
        System.out.println("\n--- 2. Key Hazard: Modifying Mutable Key in HashMap ---");

        class MutableKey {
            int id;

            MutableKey(int id) {
                this.id = id;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                MutableKey that = (MutableKey) o;
                return id == that.id;
            }

            @Override
            public int hashCode() {
                return Objects.hash(id);
            }
        }

        Map<MutableKey, String> map = new HashMap<>();
        MutableKey key1 = new MutableKey(42);
        map.put(key1, "Active Session Data");

        System.out.println("Put key1(42) -> 'Active Session Data'. Value lookup: " + map.get(key1));

        // Mutating the key after insertion!
        key1.id = 99; // Changes hashCode() calculation!

        System.out.println("Mutated key1.id to 99 after insertion into Map.");
        System.out.println("Attempting lookup with mutated key1: " + map.get(key1) + " -> NULL (MAP LOOKUP CORRUPTED!)");
        System.out.println("💡 SRE Rule: ALWAYS use IMMUTABLE classes (String, Integer, Records) as HashMap Keys!");
    }

    private static void demonstrateHashMapTreeification() {
        System.out.println("\n--- 3. HashMap Treeification Mechanics (Java 8+ Red-Black Tree Bin) ---");

        // Key class forced to collide in bucket index 0
        class CollidingKey implements Comparable<CollidingKey> {
            final int id;

            CollidingKey(int id) {
                this.id = id;
            }

            @Override
            public int hashCode() {
                return 1; // Force identical bucket placement for all instances!
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                CollidingKey other = (CollidingKey) obj;
                return this.id == other.id;
            }

            @Override
            public int compareTo(CollidingKey o) {
                return Integer.compare(this.id, o.id);
            }
        }

        HashMap<CollidingKey, String> map = new HashMap<>(64); // Min capacity 64 for treeification
        System.out.println("Populating 10 colliding keys into bucket hash=1...");

        for (int i = 1; i <= 10; i++) {
            map.put(new CollidingKey(i), "Val-" + i);
        }

        inspectHashMapBucketNodes(map);
        System.out.println("💡 SRE Insight: Treeification prevents O(n) HashDoS (Hash Denial of Service) attacks");
        System.out.println("   by ensuring lookups in heavily collided buckets bound to O(log n) time!");
    }

    private static void demonstrateLinkedHashMapLruCache() {
        System.out.println("\n--- 4. LinkedHashMap LRU (Least Recently Used) Cache ---");

        // Custom LRU Cache bounded to 3 entries using LinkedHashMap access-order mode
        int capacity = 3;
        LinkedHashMap<String, String> lruCache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > capacity;
            }
        };

        lruCache.put("user:101", "Alice");
        lruCache.put("user:102", "Bob");
        lruCache.put("user:103", "Charlie");

        System.out.println("Cache populated (Cap 3): " + lruCache.keySet());

        // Access user:101 -> Moves to most recently used position
        lruCache.get("user:101");
        System.out.println("Accessed 'user:101'. New Access Order: " + lruCache.keySet());

        // Insert user:104 -> Evicts eldest ('user:102')
        lruCache.put("user:104", "Dave");
        System.out.println("Added 'user:104'. Cache State after LRU eviction: " + lruCache.keySet());
    }

    private static void demonstrateTreeMapNavigableOperations() {
        System.out.println("\n--- 5. TreeMap & NavigableMap Range Queries ---");
        TreeMap<Integer, String> metricTimeseries = new TreeMap<>();
        metricTimeseries.put(100, "10:00 AM - CPU 20%");
        metricTimeseries.put(200, "10:05 AM - CPU 25%");
        metricTimeseries.put(300, "10:10 AM - CPU 95% (SPIKE)");
        metricTimeseries.put(400, "10:15 AM - CPU 30%");
        metricTimeseries.put(500, "10:20 AM - CPU 22%");

        System.out.println("First Entry (Earliest metric) : " + metricTimeseries.firstEntry());
        System.out.println("Last Entry (Latest metric)   : " + metricTimeseries.lastEntry());
        System.out.println("Floor Entry <= 250           : " + metricTimeseries.floorEntry(250));
        System.out.println("Ceiling Entry >= 250         : " + metricTimeseries.ceilingEntry(250));
        System.out.println("SubMap range (200 to 400 ex) : " + metricTimeseries.subMap(200, 400));
    }

    private static int getHashMapCapacity(HashMap<?, ?> map) {
        try {
            Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);
            Object[] table = (Object[]) tableField.get(map);
            return table == null ? 0 : table.length;
        } catch (Exception e) {
            return -1;
        }
    }

    private static void inspectHashMapBucketNodes(HashMap<?, ?> map) {
        try {
            Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);
            Object[] table = (Object[]) tableField.get(map);
            if (table != null) {
                for (int i = 0; i < table.length; i++) {
                    Object node = table[i];
                    if (node != null) {
                        System.out.println("  Bucket [" + i + "] Node Class: " + node.getClass().getSimpleName());
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Reflection inspection failed: " + e.getMessage());
        }
    }
}
