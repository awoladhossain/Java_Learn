package com.example.httpserver.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    @Test
    @DisplayName("Should accumulate metrics accurately across concurrent Virtual Threads")
    void testConcurrentMetricsAccumulation() throws InterruptedException {
        MetricsCollector collector = new MetricsCollector();
        int threadCount = 100;
        int requestsPerThread = 100;

        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int r = 0; r < requestsPerThread; r++) {
                            collector.recordRequest("/api/logs", 200, 150, 500_000);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        ServerMetricsDto snapshot = collector.getSnapshot();
        assertEquals(10_000, snapshot.totalRequests());
        assertEquals(1_500_000, snapshot.totalBytesTransferred());
        assertEquals(10_000L, snapshot.statusCodes().get(200));
        assertEquals(10_000L, snapshot.routeHits().get("/api/logs"));
    }
}
