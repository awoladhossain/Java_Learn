package com.example.solid.isp;

/**
 * ISP Compliance:
 * RedisCacheWorker implements ONLY CacheManageable.
 * It is not forced to provide dummy empty implementations for deployment or DB migration methods.
 */
public class RedisCacheWorker implements CacheManageable {

    private final String clusterEndpoint;

    public RedisCacheWorker(String clusterEndpoint) {
        this.clusterEndpoint = clusterEndpoint;
    }

    @Override
    public void flushAll() {
        System.out.printf("      🧹 [ISP - RedisCacheWorker] Executing FLUSHALL on %s%n", clusterEndpoint);
    }

    @Override
    public void invalidateKey(String key) {
        System.out.printf("      🗑️ [ISP - RedisCacheWorker] Invalidation key '%s' on %s%n", key, clusterEndpoint);
    }
}
