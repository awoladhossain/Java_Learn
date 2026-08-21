package com.example.serialization;

/**
 * Main Runner Class for Phase 4.2: Serialization & Data Formats.
 * 
 * Executes comprehensive demonstrations covering:
 * - 4.2.1 Native Java Serialization, serialVersionUID & transient fields.
 * - 4.2.2 Security Risks of Native Serialization & Java 9+ ObjectInputFilter allowlisting.
 * - 4.2.3 Pure Java JSON Parsing, Tokenization & Object Mapping without framework magic.
 */
public class SerializationMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 4.2: SERIALIZATION & DATA FORMATS DEEP-DIVE DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Native Java Serialization & Transient Fields
        NativeSerializationDemo.runDemo();

        // 2. Security Risks & ObjectInputFilter Mitigation
        SerializationSecurityRisksDemo.runDemo();

        // 3. Pure Java JSON Serialization & Parsing
        PureJsonSerializationDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 4.2 SERIALIZATION EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
