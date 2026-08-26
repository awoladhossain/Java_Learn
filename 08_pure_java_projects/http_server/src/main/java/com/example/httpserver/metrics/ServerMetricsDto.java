package com.example.httpserver.metrics;

import java.util.Map;

public record ServerMetricsDto(
        long totalRequests,
        long totalBytesTransferred,
        double averageLatencyMs,
        long totalLogEntriesParsed,
        long totalLogErrors,
        Map<Integer, Long> statusCodes,
        Map<String, Long> routeHits,
        long uptimeSeconds
) {}
