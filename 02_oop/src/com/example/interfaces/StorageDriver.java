package com.example.interfaces;

/**
 * Interface contract for storage drivers used in Composition Over Inheritance.
 */
public interface StorageDriver {
    void writeData(String key, byte[] data);
    byte[] readData(String key);
    String getStorageType();
}
