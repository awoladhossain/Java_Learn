package com.example.generics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Section 3.3.3: Wildcards and the PECS Principle.
 * 
 * Demonstrates:
 * - Invariance of generic types (List<Integer> is NOT List<Number>).
 * - Unbounded Wildcards (List<?>).
 * - Upper-Bounded Wildcards (List<? extends Number>) - Covariance / Producer Extends.
 * - Lower-Bounded Wildcards (List<? super Integer>) - Contravariance / Consumer Super.
 * - PECS Rule (Producer Extends, Consumer Super) in utility methods.
 */
public class WildcardsPECSDemo {

    /**
     * Unbounded Wildcard Method: List<?>
     * Can read items as Object, but CANNOT add elements (type safety guard).
     */
    public static void printCollection(Collection<?> collection) {
        System.out.print("Collection Elements [Count=" + collection.size() + "]: ");
        for (Object elem : collection) {
            System.out.print(elem + " ");
        }
        System.out.println();
        // collection.add("test"); // COMPILE ERROR! Cannot add anything except null into Collection<?>
    }

    /**
     * Upper-Bounded Wildcard: List<? extends Number> -> PRODUCER EXTENDS
     * Produces / Reads numbers from the list.
     */
    public static double sumOfNumbers(List<? extends Number> list) {
        double sum = 0.0;
        for (Number num : list) {
            sum += num.doubleValue(); // Safe read: Guaranteed to be at least a Number
        }
        // list.add(10); // COMPILE ERROR! Cannot write to ? extends Number (producer only)
        return sum;
    }

    /**
     * Lower-Bounded Wildcard: List<? super Integer> -> CONSUMER SUPER
     * Consumes / Writes Integer values into the list.
     */
    public static void populateMetricSequence(List<? super Integer> consumerList, int start, int count) {
        for (int i = 0; i < count; i++) {
            consumerList.add(start + i); // Safe write: Integer can be assigned to Integer, Number, or Object
        }
        // Object item = consumerList.get(0); // Read returns Object only (not Integer)
    }

    /**
     * PECS Canonical Implementation: Copy elements from Producer (src) to Consumer (dest).
     * 
     * @param dest Consumer (List<? super T>) - receives data written into it
     * @param src  Producer (List<? extends T>) - supplies data read out of it
     */
    public static <T> void copyPECS(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {     // Read from src (Producer Extends)
            dest.add(item);      // Write to dest (Consumer Super)
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 3.3.3 WILDCARDS & PECS: Unbounded (?), Upper (? extends), Lower (? super)");
        System.out.println("------------------------------------------------------------------------");

        // 1. Generic Invariance Paradox
        System.out.println("\n--- 1. Generic Invariance Paradox ---");
        List<Integer> intList = new ArrayList<>(List.of(10, 20, 30));
        // List<Number> numList = intList; // COMPILE ERROR! List<Integer> is NOT a subtype of List<Number>
        System.out.println("Why is List<Integer> invariant to List<Number>?");
        System.out.println("If allowed, numList.add(3.14) would inject Double into List<Integer>, breaking Heap Integrity!");

        // 2. Unbounded Wildcard List<?>
        System.out.println("\n--- 2. Unbounded Wildcard (List<?>) ---");
        List<String> stringList = List.of("nginx", "envoy", "traefik");
        printCollection(intList);
        printCollection(stringList);

        // 3. Upper-Bounded Wildcard (? extends Number) - Producer Extends
        System.out.println("\n--- 3. Upper-Bounded Wildcard (? extends Number) - Producer Extends ---");
        List<Integer> integers = List.of(100, 200, 300);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);

        double intSum = sumOfNumbers(integers); // Passed List<Integer>
        double doubleSum = sumOfNumbers(doubles); // Passed List<Double>
        System.out.println("Sum of List<Integer> : " + intSum);
        System.out.println("Sum of List<Double>  : " + doubleSum);

        // 4. Lower-Bounded Wildcard (? super Integer) - Consumer Super
        System.out.println("\n--- 4. Lower-Bounded Wildcard (? super Integer) - Consumer Super ---");
        List<Number> numberList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        populateMetricSequence(numberList, 10, 3); // Passing List<Number> as Consumer
        populateMetricSequence(objectList, 100, 3); // Passing List<Object> as Consumer

        System.out.println("Populated List<Number> : " + numberList);
        System.out.println("Populated List<Object> : " + objectList);

        // 5. PECS Rule in Action (copyPECS)
        System.out.println("\n--- 5. PECS (Producer Extends, Consumer Super) Copy Operation ---");
        List<Double> doubleProducer = List.of(10.1, 20.2, 30.3);
        List<Number> numberConsumer = new ArrayList<>();

        // Producer: doubleProducer (List<Double> fits ? extends Number)
        // Consumer: numberConsumer (List<Number> fits ? super Number)
        copyPECS(numberConsumer, doubleProducer);

        System.out.println("Source Producer (List<Double>) : " + doubleProducer);
        System.out.println("Destination Consumer (List<Number>): " + numberConsumer);

        System.out.println("\n💡 SRE Mnemonic: PECS Rule");
        System.out.println("   - Producer Extends: Use '? extends T' when reading data FROM a collection.");
        System.out.println("   - Consumer Super  : Use '? super T' when writing data INTO a collection.");
    }
}
