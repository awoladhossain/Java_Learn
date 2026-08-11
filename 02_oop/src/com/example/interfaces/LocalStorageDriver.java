package com.example.interfaces;

import java.nio.charset.StandardCharsets;

public class LocalStorageDriver implements StorageDriver {

    private final String baseDir;

    public LocalStorageDriver(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void writeData(String key, byte[] data) {
        System.out.printf("      💾 [LocalStorageDriver] Writing %d bytes to disk at %s/%s%n",
                data.length, baseDir, key);
    }

    @Override
    public byte[] readData(String key) {
        System.out.printf("      💾 [LocalStorageDriver] Reading file from %s/%s%n", baseDir, key);
        return ("Payload from Local Disk (" + key + ")").getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getStorageType() {
        return "Local FileSystem (" + baseDir + ")";
    }
}
