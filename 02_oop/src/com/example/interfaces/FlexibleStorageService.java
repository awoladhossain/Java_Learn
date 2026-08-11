package com.example.interfaces;

import java.nio.charset.StandardCharsets;

/**
 * High-Level Service demonstrating "Composition Over Inheritance" principle (Favoring 'has-a' over 'is-a').
 * 
 * Instead of extending deep inheritance trees:
 * (e.g. BaseStorageService -> S3StorageService -> EncryptedS3StorageService -> EncryptedCompressedS3StorageService)
 * 
 * This class COMPOSED of storage, encryption, and telemetry drivers via HAS-A relationships.
 * Behaviors can be swapped dynamically at runtime without changing class definitions.
 */
public class FlexibleStorageService {

    // HAS-A Relationships (Composition fields referencing Interface contracts)
    private StorageDriver storageDriver;
    private EncryptionEngine encryptionEngine;

    // Constructor Injection (Loose Coupling)
    public FlexibleStorageService(StorageDriver storageDriver, EncryptionEngine encryptionEngine) {
        this.storageDriver = storageDriver;
        this.encryptionEngine = encryptionEngine;
    }

    // Dynamic component swapping at runtime (Impossible with traditional inheritance!)
    public void setStorageDriver(StorageDriver storageDriver) {
        System.out.printf("      🔄 Swapping StorageDriver to: %s%n", storageDriver.getStorageType());
        this.storageDriver = storageDriver;
    }

    public void setEncryptionEngine(EncryptionEngine encryptionEngine) {
        System.out.printf("      🔄 Swapping EncryptionEngine to: %s%n", encryptionEngine.getAlgorithmName());
        this.encryptionEngine = encryptionEngine;
    }

    public void storeFile(String filename, String content) {
        System.out.printf("   ⚙️ Processing file '%s' via FlexibleStorageService (%s + %s):%n",
                filename, storageDriver.getStorageType(), encryptionEngine.getAlgorithmName());

        byte[] rawBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptionEngine.encrypt(rawBytes);
        storageDriver.writeData(filename, encryptedBytes);
        System.out.println("      ✅ Storage operation complete.\n");
    }

    public String retrieveFile(String filename) {
        System.out.printf("   ⚙️ Retrieving file '%s' via FlexibleStorageService (%s + %s):%n",
                filename, storageDriver.getStorageType(), encryptionEngine.getAlgorithmName());

        byte[] encryptedData = storageDriver.readData(filename);
        byte[] decryptedData = encryptionEngine.decrypt(encryptedData);
        String result = new String(decryptedData, StandardCharsets.UTF_8);
        System.out.println("      ✅ Retrieval operation complete.\n");
        return result;
    }
}
