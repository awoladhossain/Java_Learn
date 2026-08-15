package com.example.collections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * 3.2.1 List Collections Deep-Dive
 * 
 * SRE & Internals Breakdown:
 * 1. ArrayList:
 *    - Backed by an Object[] array (elementData).
 *    - Default initial capacity = 10 (when first element is added).
 *    - Growth formula: newCapacity = oldCapacity + (oldCapacity >> 1) -> 1.5x scaling factor.
 *    - Lookup by index: O(1) via direct pointer arithmetic.
 *    - Insertion/Deletion at middle/head: O(n) due to System.arraycopy memory shifting.
 * 
 * 2. LinkedList:
 *    - Doubly-linked list composed of Node<E> objects (item, next, prev).
 *    - Overhead: 24 bytes of Node object header + 3 references (24 bytes) = ~48 bytes per element on 64-bit JVM.
 *    - Traversal: O(n) pointer chasing causing CPU cache misses (poor spatial locality).
 * 
 * 3. Vector & Stack:
 *    - Legacy synchronized collections (coarse-grained method-level locking).
 *    - Vector default growth formula: 2.0x scaling factor.
 *    - Stack extends Vector (violates composition principle; exposes random access methods like add/get).
 *    - Modern replacement for Stack: Deque / ArrayDeque.
 */
public class ListDeepDiveDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.2.1 LIST COLLECTIONS: ArrayList vs LinkedList vs Vector/Stack");
        System.out.println("------------------------------------------------------------------------");

        demonstrateArrayListResizing();
        demonstrateListAccessPerformance();
        demonstrateLinkedListOverhead();
        demonstrateLegacyVectorAndStack();
    }

    private static void demonstrateArrayListResizing() {
        System.out.println("\n--- 1. ArrayList Resizing Mechanics (1.5x Factor) ---");
        ArrayList<Integer> list = new ArrayList<>();
        System.out.println("Initial empty ArrayList instantiated.");

        // Inspect capacity using Reflection on the internal 'elementData' field
        System.out.println("Internal capacity before first insertion: " + getArrayListCapacity(list));

        list.add(1);
        System.out.println("Capacity after 1st element: " + getArrayListCapacity(list) + " (Default initial capacity)");

        for (int i = 2; i <= 10; i++) {
            list.add(i);
        }
        System.out.println("Capacity with 10 elements: " + getArrayListCapacity(list));

        // Adding 11th element triggers resize: 10 + (10 >> 1) = 15
        list.add(11);
        System.out.println("Capacity after 11th element (Triggered 1.5x resize): " + getArrayListCapacity(list));

        for (int i = 12; i <= 15; i++) {
            list.add(i);
        }
        System.out.println("Capacity with 15 elements: " + getArrayListCapacity(list));

        // Adding 16th element triggers next resize: 15 + (15 >> 1) = 22
        list.add(16);
        System.out.println("Capacity after 16th element (Triggered 1.5x resize): " + getArrayListCapacity(list));

        System.out.println("\n💡 SRE Insight: Pre-allocating ArrayList capacity via new ArrayList<>(initialCapacity)");
        System.out.println("   eliminates expensive array copy operations and CPU spikes during high-throughput ingestion!");
    }

    private static void demonstrateListAccessPerformance() {
        System.out.println("\n--- 2. Performance Comparison: Random Access & Insertion ---");
        int count = 100_000;
        List<Integer> arrayList = new ArrayList<>(count);
        List<Integer> linkedList = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // Random Access Test (Index lookup at middle)
        long startTime = System.nanoTime();
        int midValArray = arrayList.get(count / 2);
        long arrayListGetTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        int midValLinked = linkedList.get(count / 2);
        long linkedListGetTime = System.nanoTime() - startTime;

        System.out.println("Random Access .get(50,000):");
        System.out.printf("  - ArrayList  : %d ns (Value: %d) -> O(1) direct offset pointer arithmetic\n", arrayListGetTime, midValArray);
        System.out.printf("  - LinkedList : %d ns (Value: %d) -> O(n) node pointer traversal (Cache Unfriendly)\n", linkedListGetTime, midValLinked);

        // Insertion at Index 0
        startTime = System.nanoTime();
        arrayList.add(0, -1);
        long arrayListInsertHeadTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        linkedList.add(0, -1);
        long linkedListInsertHeadTime = System.nanoTime() - startTime;

        System.out.println("\nInsertion at Index 0 (Head):");
        System.out.printf("  - ArrayList  : %d ns -> O(n) due to System.arraycopy memory shift\n", arrayListInsertHeadTime);
        System.out.printf("  - LinkedList : %d ns -> O(1) header pointer reassignment\n", linkedListInsertHeadTime);
    }

    private static void demonstrateLinkedListOverhead() {
        System.out.println("\n--- 3. LinkedList Memory Overhead Analysis ---");
        System.out.println("ArrayList packed memory: Contiguous primitive reference array (8 bytes per ref on 64-bit JVM).");
        System.out.println("LinkedList node memory : Object Header (16 bytes) + Item Ref (8 bytes) + Next Ref (8 bytes) + Prev Ref (8 bytes) = ~40-48 bytes per element!");
        System.out.println("💡 SRE Recommendation: Avoid LinkedList in production unless frequent head/tail operations are performed without indexed access.");
    }

    private static void demonstrateLegacyVectorAndStack() {
        System.out.println("\n--- 4. Legacy Vector & Stack (Thread Contention & Design Flaws) ---");
        Vector<String> vector = new Vector<>();
        vector.add("Production");
        vector.add("Metrics");

        System.out.println("Vector element count: " + vector.size() + " (All methods marked synchronized)");

        Stack<String> stack = new Stack<>();
        stack.push("Layer1");
        stack.push("Layer2");
        stack.pop();

        // Architectural flaw in java.util.Stack: Inherits from Vector, allowing arbitrary index insertions
        stack.add(0, "IllegalStackElement"); // Stack breaks LIFO invariant!

        System.out.println("Stack top after push/pop/add(0): " + stack.peek());
        System.out.println("⚠️ Anti-Pattern Warning: java.util.Stack extends Vector, exposing elementAt()/add(index).");
        System.out.println("💡 Best Practice: Use ArrayDeque<E> as a Stack or Queue replacement (Faster & Non-synchronized).");
    }

    private static int getArrayListCapacity(ArrayList<?> list) {
        try {
            Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] elementData = (Object[]) field.get(list);
            return elementData.length;
        } catch (Exception e) {
            return -1;
        }
    }
}
