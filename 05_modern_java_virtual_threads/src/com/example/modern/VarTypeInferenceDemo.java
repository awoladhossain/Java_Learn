package com.example.modern;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Section 5.3.2: Local Variable Type Inference (var).
 * 
 * Demonstrates:
 * - Valid usage of var: Local variable declarations, loop constructs, try-with-resources, lambda parameters (Java 11).
 * - Restrictions & Rules: Field variables, method params, method returns, uninitialized / null variables.
 * - SRE Code Quality Guidelines: Clean code without compromising type safety and readability.
 */
public class VarTypeInferenceDemo {

    public record MetricNode(String nodeName, int cpuCoreCount, double memoryGb) {}

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.3.2 LOCAL VARIABLE TYPE INFERENCE (var)");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Valid Usage of 'var'
        // ==========================================
        System.out.println("\n--- 1. Valid Usage Scenarios ---");

        // A. Simplifying complex nested generic declarations
        // Old Way: Map<String, List<MetricNode>> clusterMap = new HashMap<String, List<MetricNode>>();
        var clusterMap = new HashMap<String, List<MetricNode>>();
        clusterMap.put("us-east-1", List.of(
            new MetricNode("node-a1", 16, 64.0),
            new MetricNode("node-a2", 32, 128.0)
        ));

        System.out.println("Inferred Map Type: " + clusterMap.getClass().getName());
        System.out.println("Cluster Map Entries: " + clusterMap);

        // B. Enhanced for-each loop
        System.out.println("Looping with 'var':");
        for (var entry : clusterMap.entrySet()) {
            var region = entry.getKey();
            var nodes = entry.getValue();
            System.out.printf("   Region [%s] has %d active nodes\n", region, nodes.size());
        }

        // C. Standard index-based loop
        var nodeNames = new ArrayList<String>();
        for (var i = 0; i < 3; i++) {
            nodeNames.add("pod-replica-" + i);
        }
        System.out.println("Generated Pod Replicas: " + nodeNames);

        // D. Try-with-resources construct
        System.out.println("Try-With-Resources with 'var':");
        String logContent = "2026-08-22 12:00:00 [INFO] System initialized\n2026-08-22 12:00:01 [WARN] High memory usage";
        
        try (var reader = new BufferedReader(new StringReader(logContent))) {
            var line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println("   [LOG-READ] " + line);
            }
        } catch (Exception e) {
            System.err.println("Error reading logs: " + e.getMessage());
        }

        // E. Lambda parameters (Java 11 allows 'var' for explicit lambda param annotations)
        java.util.function.BiFunction<String, String, String> joiner = (var s1, var s2) -> s1 + ":" + s2;
        System.out.println("Lambda with var parameters: " + joiner.apply("http", "8080"));

        // ==========================================
        // 2. Rules & Restrictions for 'var'
        // ==========================================
        System.out.println("\n--- 2. Rules & Compilation Constraints ---");
        System.out.println("   ✓ 'var' is STILL strongly typed at compile time (NOT dynamic typing like JavaScript 'var' or Python).");
        System.out.println("   ✓ Compiler infers static type from the right-hand side initialization expression.");
        System.out.println("   ✘ CANNOT be used for class fields (instance or static variables).");
        System.out.println("   ✘ CANNOT be used for method return types or method parameter signatures.");
        System.out.println("   ✘ CANNOT be left uninitialized (e.g. 'var x;' -> COMPILE ERROR).");
        System.out.println("   ✘ CANNOT be initialized to null (e.g. 'var x = null;' -> COMPILE ERROR).");
        System.out.println("   ✘ CANNOT be used with array initializer syntax (e.g. 'var arr = {1, 2};' -> COMPILE ERROR, must use 'new int[]{1, 2}').");

        // Demonstrate compile-time type preservation
        var integerVar = 100; // Inferred as int
        // integerVar = "Hello"; // COMPILE ERROR: Incompatible types!
        System.out.println("Inferred Integer Type Value: " + integerVar);

        // ==========================================
        // 3. Senior SRE Code Quality Guidelines
        // ==========================================
        System.out.println("\n💡 Senior SRE Best Practices for 'var':");
        System.out.println("   1. Use 'var' when it eliminates noisy repetition (e.g. 'var map = new HashMap<String, List<Node>>()').");
        System.out.println("   2. AVOID 'var' when the right-hand side expression is opaque (e.g. 'var result = processData()' hides return type).");
        System.out.println("   3. Always use descriptive variable names when using 'var' to maintain code readability during pull reviews.");
    }
}
