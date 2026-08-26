package com.example.httpserver.log;

import java.time.Instant;

public record LogEntry(
        String id,
        Instant timestamp,
        LogLevel level,
        String clientIp,
        String method,
        String endpoint,
        int statusCode,
        long responseTimeMs,
        String message
) {}
