package com.example.interfaces;

public class NoOpEncryptionEngine implements EncryptionEngine {

    @Override
    public byte[] encrypt(byte[] rawData) {
        System.out.println("      ℹ️ [NoOpEncryption] Pass-through without encryption.");
        return rawData;
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) {
        System.out.println("      ℹ️ [NoOpEncryption] Pass-through without decryption.");
        return encryptedData;
    }

    @Override
    public String getAlgorithmName() {
        return "NONE (Plaintext)";
    }
}
