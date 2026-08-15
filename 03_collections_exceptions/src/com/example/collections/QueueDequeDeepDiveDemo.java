package com.example.collections;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 3.2.4 Queues & Deques Deep-Dive
 * 
 * SRE & Internals Breakdown:
 * 1. ArrayDeque:
 *    - Circular array buffer implementation (elements[], head, tail pointers).
 *    - Functions as both Stack (LIFO) and Queue (FIFO).
 *    - Rejects null elements (throws NullPointerException).
 *    - Memory efficiency: Contiguous array allocation without Node object overhead; faster than LinkedList & Stack.
 * 
 * 2. PriorityQueue:
 *    - Unbounded priority queue backed by a binary Min-Heap / Max-Heap array representation.
 *    - Root (queue[0]) holds the minimum (or maximum) element.
 *    - Enqueue (offer) and Dequeue (poll) execute in O(log n) time via siftUp and siftDown heapify operations.
 * 
 * 3. BlockingQueue:
 *    - Concurrency contract for thread-safe producer-consumer patterns.
 *    - put(e) blocks when capacity is reached; take() blocks when queue is empty.
 *    - Implementations: ArrayBlockingQueue (fixed bounded array with ReentrantLock), LinkedBlockingQueue (optionally bounded).
 */
public class QueueDequeDeepDiveDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.2.4 QUEUES & DEQUES: ArrayDeque, PriorityQueue & BlockingQueue");
        System.out.println("------------------------------------------------------------------------");

        demonstrateArrayDequeCircularBuffer();
        demonstratePriorityQueueMinMaxHeap();
        demonstrateBlockingQueueProducerConsumer();
    }

    private static void demonstrateArrayDequeCircularBuffer() {
        System.out.println("\n--- 1. ArrayDeque: Double-Ended Circular Array Buffer ---");
        Deque<String> deque = new ArrayDeque<>();

        // FIFO Queue operations
        deque.offerLast("Job-1");
        deque.offerLast("Job-2");
        deque.offerLast("Job-3");
        System.out.println("Enqueued 3 jobs FIFO. PollFirst: " + deque.pollFirst() + " (Remaining: " + deque + ")");

        // LIFO Stack operations
        deque.push("HighPriorityTask"); // push is alias for addFirst
        System.out.println("Pushed high-priority stack item. Peek: " + deque.peek() + " (Stack view: " + deque + ")");
        System.out.println("Popped stack item: " + deque.pop());

        try {
            deque.add(null);
        } catch (NullPointerException e) {
            System.out.println("Caught NPE attempting to insert null into ArrayDeque -> Null values explicitly forbidden.");
        }

        System.out.println("💡 SRE Recommendation: ArrayDeque is the DE FACTO standard choice for single-threaded Stacks & Queues.");
    }

    private static void demonstratePriorityQueueMinMaxHeap() {
        System.out.println("\n--- 2. PriorityQueue: Binary Heap Mechanics ---");

        // Record representing an alert ticket with severity weight
        record Alert(String title, int severityScore) implements Comparable<Alert> {
            @Override
            public int compareTo(Alert other) {
                // Higher severityScore = higher priority (Max-Heap)
                return Integer.compare(other.severityScore, this.severityScore);
            }
        }

        Queue<Alert> maxHeapAlerts = new PriorityQueue<>();
        maxHeapAlerts.offer(new Alert("Disk Usage 70%", 3));
        maxHeapAlerts.offer(new Alert("DATABASE OUTAGE", 10));
        maxHeapAlerts.offer(new Alert("High Latency Warning", 6));
        maxHeapAlerts.offer(new Alert("CPU Warning 80%", 5));

        System.out.println("Alerts enqueued into Max-Heap PriorityQueue (Total: 4)");
        System.out.println("Polling alerts by priority (Highest severityScore processed first):");

        while (!maxHeapAlerts.isEmpty()) {
            Alert alert = maxHeapAlerts.poll();
            System.out.println("  🚨 Processing Alert: " + alert.title + " [Severity Score: " + alert.severityScore + "]");
        }

        // Min-Heap using explicit Comparator
        Queue<Integer> minHeap = new PriorityQueue<>(Comparator.naturalOrder());
        minHeap.offer(45);
        minHeap.offer(12);
        minHeap.offer(89);
        minHeap.offer(3);

        System.out.println("\nMin-Heap PriorityQueue Polling Order: ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " ");
        }
        System.out.println();
    }

    private static void demonstrateBlockingQueueProducerConsumer() {
        System.out.println("\n--- 3. BlockingQueue: Thread-Safe Producer-Consumer Pattern ---");
        BlockingQueue<String> logChannel = new ArrayBlockingQueue<>(2); // Bounded queue capacity = 2

        Thread producer = new Thread(() -> {
            try {
                String[] logs = {"[INFO] App started", "[WARN] High Memory", "[ERROR] Connection Reset"};
                for (String log : logs) {
                    System.out.println("  [Producer] Putting: " + log);
                    logChannel.put(log); // Blocks when capacity 2 is reached until consumer drains!
                    System.out.println("  [Producer] Successfully put: " + log);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    TimeUnit.MILLISECONDS.sleep(300); // Simulate processing latency
                    String log = logChannel.take(); // Blocks if queue is empty!
                    System.out.println("  [Consumer] Processed: " + log);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("💡 SRE Takeaway: ArrayBlockingQueue is fundamental to ThreadPoolExecutor task queues.");
    }
}
