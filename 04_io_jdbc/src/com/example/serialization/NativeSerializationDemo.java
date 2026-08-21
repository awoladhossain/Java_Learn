package com.example.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Section 4.2.1: Native Java Serialization & Transient Fields.
 * 
 * Demonstrates:
 * - Serializable interface marker.
 * - Explicit serialVersionUID for version compatibility across deployments.
 * - transient keyword for excluding sensitive/volatile fields (e.g. passwords, tokens).
 * - Custom serialization methods (writeObject / readObject).
 */
public class NativeSerializationDemo {

    /**
     * Domain class implementing Serializable with explicit serialVersionUID and transient fields.
     */
    public static class UserSession implements Serializable {
        // Explicit version UID to prevent InvalidClassException if class structure evolves slightly
        private static final long serialVersionUID = 1001L;

        private final String sessionId;
        private final String username;
        private final long creationTime;

        // Transient fields: Will NOT be written to bytecode stream
        private transient String cleartextPassword;
        private transient String bearerToken;
        private transient int activeConnectionCount;

        public UserSession(String sessionId, String username, String cleartextPassword, String bearerToken) {
            this.sessionId = sessionId;
            this.username = username;
            this.cleartextPassword = cleartextPassword;
            this.bearerToken = bearerToken;
            this.creationTime = System.currentTimeMillis();
            this.activeConnectionCount = 5;
        }

        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public long getCreationTime() { return creationTime; }
        public String getCleartextPassword() { return cleartextPassword; }
        public String getBearerToken() { return bearerToken; }
        public int getActiveConnectionCount() { return activeConnectionCount; }

        @Override
        public String toString() {
            return String.format("UserSession[id=%s, user=%s, created=%d, pass=%s, token=%s, conns=%d]",
                    sessionId, username, creationTime, cleartextPassword, bearerToken, activeConnectionCount);
        }
    }

    /**
     * Domain class overriding writeObject and readObject for custom encryption/masking during serialization.
     */
    public static class EncryptedApiKeyPayload implements Serializable {
        private static final long serialVersionUID = 2002L;

        private String serviceName;
        private transient String secretKey; // Protected transient field

        public EncryptedApiKeyPayload(String serviceName, String secretKey) {
            this.serviceName = serviceName;
            this.secretKey = secretKey;
        }

        public String getServiceName() { return serviceName; }
        public String getSecretKey() { return secretKey; }

        /**
         * Custom writeObject logic triggered automatically by ObjectOutputStream.
         */
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject(); // Serialize non-transient fields (serviceName)
            // Mask/encrypt secret key before writing to output stream
            String obfuscatedKey = "MASKED-" + new StringBuilder(secretKey).reverse().toString();
            out.writeUTF(obfuscatedKey);
        }

        /**
         * Custom readObject logic triggered automatically by ObjectInputStream.
         */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject(); // Deserialize non-transient fields
            String obfuscatedKey = in.readUTF();
            // De-obfuscate secret key
            String reversed = obfuscatedKey.replace("MASKED-", "");
            this.secretKey = new StringBuilder(reversed).reverse().toString();
        }

        @Override
        public String toString() {
            return String.format("EncryptedApiKeyPayload[service=%s, secretKey=%s]", serviceName, secretKey);
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.2.1 NATIVE JAVA SERIALIZATION & TRANSIENT FIELDS");
        System.out.println("------------------------------------------------------------------------");

        try {
            // 1. Native Serialization & Transient Field Reset
            System.out.println("\n--- 1. Native Object Serialization & Transient Behavior ---");
            UserSession originalSession = new UserSession(
                    "sess-99821", "sre_operator", "SuperSecretPass123!", "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
            );

            System.out.println("Original Session Object  : " + originalSession);

            // Serialize Object to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(originalSession);
            }

            byte[] serializedBytes = baos.toByteArray();
            System.out.printf("Serialized Byte Stream   : %d bytes generated\n", serializedBytes.length);

            // Deserialize Object from byte array
            UserSession deserializedSession;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedBytes))) {
                deserializedSession = (UserSession) ois.readObject();
            }

            System.out.println("Deserialized Session     : " + deserializedSession);
            System.out.println("  - Non-transient Username : " + deserializedSession.getUsername());
            System.out.println("  - Transient Password     : " + deserializedSession.getCleartextPassword() + " (RESET TO NULL!)");
            System.out.println("  - Transient Bearer Token : " + deserializedSession.getBearerToken() + " (RESET TO NULL!)");
            System.out.println("  - Transient Conn Count   : " + deserializedSession.getActiveConnectionCount() + " (RESET TO DEFAULT 0!)");

            // 2. Custom writeObject / readObject Hooks
            System.out.println("\n--- 2. Custom writeObject / readObject Serialization Hooks ---");
            EncryptedApiKeyPayload originalPayload = new EncryptedApiKeyPayload("payment-gateway", "sk_live_9948271048");
            System.out.println("Original Payload Object  : " + originalPayload);

            ByteArrayOutputStream customBaos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(customBaos)) {
                oos.writeObject(originalPayload);
            }

            EncryptedApiKeyPayload restoredPayload;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(customBaos.toByteArray()))) {
                restoredPayload = (EncryptedApiKeyPayload) ois.readObject();
            }

            System.out.println("Restored Payload Object  : " + restoredPayload);
            System.out.println("  - Restored Secret Key  : " + restoredPayload.getSecretKey() + " (SUCCESSFULLY RECONSTRUCTED!)");

        } catch (Exception e) {
            System.err.println("Serialization Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n💡 SRE Best Practice:");
        System.out.println("   ALWAYS explicitly define 'private static final long serialVersionUID = ...;'.");
        System.out.println("   Without it, javac automatically generates a hash based on class structure; any minor code change");
        System.out.println("   will cause an InvalidClassException when deserializing previously saved objects!");
    }
}
