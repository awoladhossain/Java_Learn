package com.example.httpserver.metrics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class MetricsCollector {
    private final Instant startTime = Instant.now();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalBytesTransferred = new LongAdder();
    private final LongAdder totalLatencyNs = new LongAdder();
    private final LongAdder logEntriesParsed = new LongAdder();
    private final LongAdder logErrors = new LongAdder();

    private final ConcurrentHashMap<Integer, LongAdder> statusCodeCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> routeHitCounts = new ConcurrentHashMap<>();

    public void recordRequest(String route, int statusCode, long bytes, long latencyNs) {
        totalRequests.increment();
        totalBytesTransferred.add(bytes);
        totalLatencyNs.add(latencyNs);

        statusCodeCounts.computeIfAbsent(statusCode, k -> new LongAdder()).increment();
        if (route != null && !route.isBlank()) {
            routeHitCounts.computeIfAbsent(route, k -> new LongAdder()).increment();
        }
    }

    public void recordLogParsed(long count, long errors) {
        logEntriesParsed.add(count);
        logErrors.add(errors);
    }

    public ServerMetricsDto getSnapshot() {
        long requests = totalRequests.sum();
        long bytes = totalBytesTransferred.sum();
        long latency = totalLatencyNs.sum();
        double avgLatencyMs = requests > 0 ? (latency / 1_000_000.0) / requests : 0.0;
        long uptime = Instant.now().getEpochSecond() - startTime.getEpochSecond();

        Map<Integer, Long> statusMap = new HashMap<>();
        statusCodeCounts.forEach((code, adder) -> statusMap.put(code, adder.sum()));

        Map<String, Long> routeMap = new HashMap<>();
        routeHitCounts.forEach((route, adder) -> routeMap.put(route, adder.sum()));

        return new ServerMetricsDto(
                requests,
                bytes,
                avgLatencyMs,
                logEntriesParsed.sum(),
                logErrors.sum(),
                statusMap,
                routeMap,
                uptime
        );
    }

    public void reset() {
        totalRequests.reset();
        totalBytesTransferred.reset();
        totalLatencyNs.reset();
        logEntriesParsed.reset();
        logErrors.reset();
        statusCodeCounts.clear();
        routeHitCounts.clear();
    }
}
