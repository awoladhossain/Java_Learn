package com.example.solid.isp;

public interface CacheManageable {
    void flushAll();
    void invalidateKey(String key);
}
