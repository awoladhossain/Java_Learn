package com.example.httpserver.log;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LogParser {

    // Format: [2026-08-26T22:00:00Z] [INFO] [127.0.0.1] GET /api/logs 200 45ms - Successfully processed request
    private static final Pattern STRUCTURED_LOG_PATTERN = Pattern.compile(
            "^\\[(?<timestamp>[^\\]]+)\\]\\s+\\[(?<level>[^\\]]+)\\]\\s+\\[(?<ip>[^\\]]+)\\]\\s+(?<method>[A-Z]+)\\s+(?<endpoint>\\S+)\\s+(?<status>\\d{3})\\s+(?<time>\\d+)ms\\s+-\\s+(?<message>.*)$"
    );

    // Fallback format: 2026-08-26T22:00:00Z INFO User logged in
    private static final Pattern SIMPLE_LOG_PATTERN = Pattern.compile(
            "^(?<timestamp>\\S+)\\s+(?<level>TRACE|DEBUG|INFO|WARN|ERROR|FATAL)\\s+(?<message>.*)$"
    );

    public Optional<LogEntry> parseLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        String trimmed = line.trim();

        Matcher structMatcher = STRUCTURED_LOG_PATTERN.matcher(trimmed);
        if (structMatcher.matches()) {
            try {
                Instant ts = parseInstant(structMatcher.group("timestamp"));
                LogLevel level = LogLevel.fromString(structMatcher.group("level"));
                String ip = structMatcher.group("ip");
                String method = structMatcher.group("method");
                String endpoint = structMatcher.group("endpoint");
                int status = Integer.parseInt(structMatcher.group("status"));
                long timeMs = Long.parseLong(structMatcher.group("time"));
                String message = structMatcher.group("message");

                return Optional.of(new LogEntry(
                        UUID.randomUUID().toString(),
                        ts, level, ip, method, endpoint, status, timeMs, message
                ));
            } catch (Exception e) {
                // Fall through to fallback or empty
            }
        }

        Matcher simpleMatcher = SIMPLE_LOG_PATTERN.matcher(trimmed);
        if (simpleMatcher.matches()) {
            try {
                Instant ts = parseInstant(simpleMatcher.group("timestamp"));
                LogLevel level = LogLevel.fromString(simpleMatcher.group("level"));
                String message = simpleMatcher.group("message");

                return Optional.of(new LogEntry(
                        UUID.randomUUID().toString(),
                        ts, level, "0.0.0.0", "UNKNOWN", "/", 200, 0, message
                ));
            } catch (Exception e) {
                // Fall through
            }
        }

        // Generic fallback for unformatted log line
        return Optional.of(new LogEntry(
                UUID.randomUUID().toString(),
                Instant.now(), LogLevel.INFO, "0.0.0.0", "UNKNOWN", "/", 200, 0, trimmed
        ));
    }

    public List<LogEntry> parseStream(Stream<String> lineStream) {
        if (lineStream == null) return List.of();
        return lineStream
                .map(this::parseLine)
                .flatMap(Optional::stream)
                .toList();
    }

    private Instant parseInstant(String rawTs) {
        try {
            return Instant.parse(rawTs);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
