package com.example.collections;

/**
 * Main Runner Class for Phase 3.2: Java Collections Framework (JCF) Deep-Dive.
 * 
 * Executes comprehensive demonstrations covering:
 * - 3.2.1 Lists: ArrayList (1.5x resizing), LinkedList (node overhead), Vector/Stack mechanics.
 * - 3.2.2 Sets: HashSet (HashMap backing), LinkedHashSet (insertion order), TreeSet (Red-Black Tree, Comparable vs Comparator).
 * - 3.2.3 Maps: HashMap internal mechanics (Buckets, hashCode distribution, equals contract, loadFactor 0.75, Treeification), LinkedHashMap (LRU cache), TreeMap.
 * - 3.2.4 Queues/Deques: ArrayDeque (circular buffer), PriorityQueue (Min/Max Heap), BlockingQueue (Producer-Consumer).
 * - 3.2.5 Thread-Safe Collections: Collections.synchronizedMap(), ConcurrentHashMap (CAS & bucket lock), CopyOnWriteArrayList.
 */
public class CollectionsMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 3.2: JAVA COLLECTIONS FRAMEWORK (JCF) DEEP-DIVE DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Lists Deep-Dive
        ListDeepDiveDemo.runDemo();

        // 2. Sets Deep-Dive
        SetDeepDiveDemo.runDemo();

        // 3. Maps Deep-Dive
        MapDeepDiveDemo.runDemo();

        // 4. Queues & Deques Deep-Dive
        QueueDequeDeepDiveDemo.runDemo();

        // 5. Thread-Safe Collections Deep-Dive
        ThreadSafeCollectionsDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 3.2 JCF DEEP-DIVE EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
