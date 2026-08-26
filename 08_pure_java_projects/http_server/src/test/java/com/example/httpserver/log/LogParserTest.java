package com.example.httpserver.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {

    private final LogParser parser = new LogParser();

    @Test
    @DisplayName("Should parse structured bracketed log line correctly")
    void testParseStructuredLogLine() {
        String logLine = "[2026-08-26T22:00:00Z] [ERROR] [192.168.1.50] POST /api/payment 500 120ms - Database timeout";

        Optional<LogEntry> result = parser.parseLine(logLine);
        assertTrue(result.isPresent());

        LogEntry entry = result.get();
        assertEquals(LogLevel.ERROR, entry.level());
        assertEquals("192.168.1.50", entry.clientIp());
        assertEquals("POST", entry.method());
        assertEquals("/api/payment", entry.endpoint());
        assertEquals(500, entry.statusCode());
        assertEquals(120, entry.responseTimeMs());
        assertEquals("Database timeout", entry.message());
    }

    @Test
    @DisplayName("Should parse stream of log lines")
    void testParseStream() {
        Stream<String> lines = Stream.of(
                "[2026-08-26T22:00:01Z] [INFO] [127.0.0.1] GET /health 200 5ms - OK",
                "[2026-08-26T22:00:02Z] [WARN] [10.0.0.5] GET /admin 403 10ms - Access denied"
        );

        List<LogEntry> entries = parser.parseStream(lines);
        assertEquals(2, entries.size());
        assertEquals(LogLevel.INFO, entries.get(0).level());
        assertEquals(LogLevel.WARN, entries.get(1).level());
    }
}
