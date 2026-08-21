package com.example.generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Section 3.3.1: Generic Classes, Interfaces, and Methods.
 * 
 * Demonstrates:
 * - Generic Class with multiple type parameters (ResponseEnvelope<T, M>).
 * - Generic Interface (GenericRepository<K, V>) and its implementation.
 * - Static and Instance Generic Methods (<T> T getOrDefault, <T, R> List<R> map).
 * - Compile-time Type Safety advantages over raw types / Object casting.
 */
public class GenericBasicsDemo {

    /**
     * Generic Interface defining contract for Key-Value data access.
     */
    public interface GenericRepository<K, V> {
        void save(K key, V value);
        V findById(K key);
        List<V> findAll();
        boolean containsKey(K key);
    }

    /**
     * Concrete implementation of GenericRepository using underlying HashMap.
     */
    public static class InMemoryRepository<K, V> implements GenericRepository<K, V> {
        private final Map<K, V> store = new HashMap<>();

        @Override
        public void save(K key, V value) {
            store.put(Objects.requireNonNull(key, "Key cannot be null"), value);
        }

        @Override
        public V findById(K key) {
            return store.get(key);
        }

        @Override
        public List<V> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public boolean containsKey(K key) {
            return store.containsKey(key);
        }
    }

    /**
     * Generic Response Envelope class for API / RPC payload standardization.
     */
    public static class ResponseEnvelope<T, M> {
        private final int statusCode;
        private final T payload;
        private final M metadata;
        private final long timestamp;

        public ResponseEnvelope(int statusCode, T payload, M metadata) {
            this.statusCode = statusCode;
            this.payload = payload;
            this.metadata = metadata;
            this.timestamp = System.currentTimeMillis();
        }

        public int getStatusCode() { return statusCode; }
        public T getPayload() { return payload; }
        public M getMetadata() { return metadata; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("ResponseEnvelope[status=%d, payload=%s, meta=%s, ts=%d]",
                    statusCode, payload, metadata, timestamp);
        }
    }

    /**
     * Generic Utility Methods.
     */
    public static class GenericUtils {

        /**
         * Generic method to swap two elements in an array.
         */
        public static <T> void swap(T[] array, int idx1, int idx2) {
            if (array == null || idx1 < 0 || idx2 < 0 || idx1 >= array.length || idx2 >= array.length) {
                throw new IndexOutOfBoundsException("Invalid swap indices for array length " + (array != null ? array.length : 0));
            }
            T temp = array[idx1];
            array[idx1] = array[idx2];
            array[idx2] = temp;
        }

        /**
         * Generic method transforming a list of input elements to a list of output elements.
         */
        public static <T, R> List<R> mapList(List<T> inputList, Function<T, R> mapper) {
            List<R> result = new ArrayList<>(inputList.size());
            for (T item : inputList) {
                result.add(mapper.apply(item));
            }
            return result;
        }

        /**
         * Generic method returning value if non-null, else fallback value.
         */
        public static <T> T getOrDefault(T value, T defaultValue) {
            return value != null ? value : defaultValue;
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.3.1 GENERICS BASICS: Classes, Interfaces, & Methods (<T>, <K, V>)");
        System.out.println("------------------------------------------------------------------------");

        // 1. Generic Repository Instance
        System.out.println("\n--- 1. Generic Interface & Class (InMemoryRepository<String, ServerNode>) ---");
        record ServerNode(String id, String ipAddress, int port) {}
        
        GenericRepository<String, ServerNode> serverRepo = new InMemoryRepository<>();
        serverRepo.save("node-1", new ServerNode("node-1", "192.168.1.10", 8080));
        serverRepo.save("node-2", new ServerNode("node-2", "192.168.1.11", 8080));

        ServerNode node = serverRepo.findById("node-1"); // No explicit casting required!
        System.out.println("Retrieved ServerNode: " + node);
        System.out.println("All stored nodes     : " + serverRepo.findAll());

        // 2. Generic Multi-Parameter Class (ResponseEnvelope<T, M>)
        System.out.println("\n--- 2. Generic Envelope (ResponseEnvelope<List<ServerNode>, Map<String, Object>>) ---");
        Map<String, Object> meta = Map.of("requestId", "req-99482", "executionTimeMs", 12);
        ResponseEnvelope<List<ServerNode>, Map<String, Object>> response = 
                new ResponseEnvelope<>(200, serverRepo.findAll(), meta);
        
        System.out.println("Envelope Status  : " + response.getStatusCode());
        System.out.println("Payload Count    : " + response.getPayload().size());
        System.out.println("Envelope Details : " + response);

        // 3. Generic Utility Methods
        System.out.println("\n--- 3. Generic Utility Methods (<T> swap, <T, R> mapList) ---");
        String[] clusterNodes = {"k8s-master", "k8s-worker-1", "k8s-worker-2"};
        System.out.println("Before Swap : " + String.join(", ", clusterNodes));
        GenericUtils.swap(clusterNodes, 0, 2);
        System.out.println("After Swap  : " + String.join(", ", clusterNodes));

        List<Integer> portNumbers = List.of(80, 443, 8080, 9092);
        List<String> formattedPorts = GenericUtils.mapList(portNumbers, port -> "Port:" + port);
        System.out.println("Mapped Ports: " + formattedPorts);

        String configHost = GenericUtils.getOrDefault(null, "localhost");
        System.out.println("Fallback Host: " + configHost);

        System.out.println("\n💡 SRE Insight: Generic classes and methods enforce strong compile-time type checking,");
        System.out.println("   eliminating ClassCastException risks and avoiding heap allocation overhead from primitive boxing when used properly.");
    }
}
