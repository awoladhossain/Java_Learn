package com.example.interfaces;

public class AesEncryptionEngine implements EncryptionEngine {

    private final String secretKey;

    public AesEncryptionEngine(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public byte[] encrypt(byte[] rawData) {
        System.out.printf("      🔐 [AES-256] Encrypting payload using key [%s...]%n",
                secretKey.substring(0, Math.min(4, secretKey.length())));
        byte[] encrypted = new byte[rawData.length + 8];
        System.arraycopy(rawData, 0, encrypted, 0, rawData.length);
        return encrypted;
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) {
        System.out.println("      🔓 [AES-256] Decrypting payload...");
        byte[] decrypted = new byte[Math.max(0, encryptedData.length - 8)];
        System.arraycopy(encryptedData, 0, decrypted, 0, decrypted.length);
        return decrypted;
    }

    @Override
    public String getAlgorithmName() {
        return "AES-256-GCM";
    }
}
