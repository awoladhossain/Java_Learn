package com.example.interfaces;

import java.nio.charset.StandardCharsets;

public class S3StorageDriver implements StorageDriver {

    private final String bucketName;

    public S3StorageDriver(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public void writeData(String key, byte[] data) {
        System.out.printf("      ☁️ [S3StorageDriver] Uploading %d bytes to s3://%s/%s%n",
                data.length, bucketName, key);
    }

    @Override
    public byte[] readData(String key) {
        System.out.printf("      ☁️ [S3StorageDriver] Fetching object from s3://%s/%s%n", bucketName, key);
        return ("Payload from S3 (" + key + ")").getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getStorageType() {
        return "AWS S3 (" + bucketName + ")";
    }
}
