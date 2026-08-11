package com.example.interfaces;

/**
 * Strategy contract for encryption in composition setup.
 */
public interface EncryptionEngine {
    byte[] encrypt(byte[] rawData);
    byte[] decrypt(byte[] encryptedData);
    String getAlgorithmName();
}
