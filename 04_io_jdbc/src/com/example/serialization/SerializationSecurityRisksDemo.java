package com.example.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Section 4.2.2: Security Risks of Java Native Serialization & Mitigation.
 * 
 * Demonstrates:
 * - Why Native ObjectInputStream deserialization is dangerous (Arbitrary Object Instantiation / Gadget Chains).
 * - Java 9+ Serialization Filtering via ObjectInputFilter (Class Allowlisting / Denylisting).
 * - Best practices for securing Java backend applications.
 */
public class SerializationSecurityRisksDemo {

    /**
     * Allowed benign class.
     */
    public static class SafeDomainModel implements Serializable {
        private static final long serialVersionUID = 5555L;
        private final String data;

        public SafeDomainModel(String data) {
            this.data = data;
        }

        public String getData() { return data; }

        @Override
        public String toString() {
            return "SafeDomainModel[data='" + data + "']";
        }
    }

    /**
     * Potentially dangerous payload class (simulating a gadget chain class).
     */
    public static class MaliciousGadgetPayload implements Serializable {
        private static final long serialVersionUID = 6666L;
        private final String payloadCommand;

        public MaliciousGadgetPayload(String payloadCommand) {
            this.payloadCommand = payloadCommand;
        }

        private void readObject(ObjectInputStream in) throws Exception {
            in.defaultReadObject();
            System.err.println("🔥 [EXPLOIT SIMULATION] Executing payload: " + payloadCommand);
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.2.2 SECURITY RISKS OF NATIVE SERIALIZATION & ObjectInputFilter");
        System.out.println("------------------------------------------------------------------------");

        try {
            // 1. Generate benign and malicious serialized payloads
            byte[] safePayloadBytes = serializeObject(new SafeDomainModel("Valid Audit Event Data"));
            byte[] dangerousPayloadBytes = serializeObject(new MaliciousGadgetPayload("rm -rf /tmp/test"));

            // 2. Unfiltered Deserialization Vulnerability Hazard
            System.out.println("\n--- 1. Hazard: Unfiltered ObjectInputStream Deserialization ---");
            System.out.println("Deserializing untrusted payload WITHOUT ObjectInputFilter:");
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dangerousPayloadBytes))) {
                Object obj = ois.readObject();
                System.out.println("  Read object: " + obj.getClass().getName());
            } catch (Exception e) {
                System.out.println("  Caught exception during unfiltered read: " + e.getMessage());
            }

            // 3. Mitigation: Java 9+ ObjectInputFilter Class Allowlisting
            System.out.println("\n--- 2. Mitigation: ObjectInputFilter Class Allowlisting ---");
            // Pattern string: Allow SafeDomainModel, deny everything else (!*)
            String filterPattern = "com.example.serialization.NativeSerializationDemo$*;com.example.serialization.SerializationSecurityRisksDemo$SafeDomainModel;!*";
            ObjectInputFilter allowlistFilter = ObjectInputFilter.Config.createFilter(filterPattern);

            System.out.println("Created Filter Pattern: '" + filterPattern + "'");

            // Attempt 1: Deserializing Safe Object with Allowlist Filter
            System.out.println("\nAttempting deserialization of SafeDomainModel with Filter:");
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(safePayloadBytes))) {
                ois.setObjectInputFilter(allowlistFilter);
                SafeDomainModel safeObj = (SafeDomainModel) ois.readObject();
                System.out.println("✅ Allowed Deserialization Success: " + safeObj);
            }

            // Attempt 2: Deserializing Malicious Object with Allowlist Filter
            System.out.println("\nAttempting deserialization of MaliciousGadgetPayload with Filter:");
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dangerousPayloadBytes))) {
                ois.setObjectInputFilter(allowlistFilter);
                Object obj = ois.readObject();
                System.out.println("Read object: " + obj);
            } catch (SecurityException | InvalidClassException e) {
                System.out.println("🛡️ SECURITY BLOCK! Filter rejected untrusted class: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Security Demo Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n💡 Senior SRE Security Rule:");
        System.out.println("   Native Java Serialization is considered an ARCHITECTURAL SECURITY LIABILITY.");
        System.out.println("   Oracle & OWASP recommend disabling ObjectInputStream across enterprise services.");
        System.out.println("   Use modern, safe data interchange formats (JSON, Protocol Buffers, Avro) instead.");
    }

    private static byte[] serializeObject(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }
}
