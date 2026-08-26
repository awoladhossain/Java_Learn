package com.example.httpserver.log;

import com.example.httpserver.metrics.MetricsCollector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogAnalyzerService {
    private final List<LogEntry> indexedLogs = new CopyOnWriteArrayList<>();
    private final LogParser logParser = new LogParser();
    private final MetricsCollector metricsCollector;

    public LogAnalyzerService(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    public List<LogEntry> ingestRawLogs(String rawLogPayload) {
        if (rawLogPayload == null || rawLogPayload.isBlank()) {
            return List.of();
        }

        String[] lines = rawLogPayload.split("\\r?\\n");
        List<LogEntry> parsed = logParser.parseStream(Stream.of(lines));
        indexedLogs.addAll(parsed);

        long errorCount = parsed.stream()
                .filter(l -> l.level() == LogLevel.ERROR || l.level() == LogLevel.FATAL || l.statusCode() >= 500)
                .count();

        if (metricsCollector != null) {
            metricsCollector.recordLogParsed(parsed.size(), errorCount);
        }

        return parsed;
    }

    public void addEntry(LogEntry entry) {
        if (entry != null) {
            indexedLogs.add(entry);
            if (metricsCollector != null) {
                long err = (entry.level() == LogLevel.ERROR || entry.statusCode() >= 500) ? 1 : 0;
                metricsCollector.recordLogParsed(1, err);
            }
        }
    }

    public List<LogEntry> queryLogs(LogLevel level, Integer minStatusCode, String keyword, int limit) {
        return indexedLogs.stream()
                .filter(entry -> level == null || entry.level() == level)
                .filter(entry -> minStatusCode == null || entry.statusCode() >= minStatusCode)
                .filter(entry -> keyword == null || keyword.isBlank()
                        || entry.message().toLowerCase().contains(keyword.toLowerCase())
                        || entry.endpoint().toLowerCase().contains(keyword.toLowerCase()))
                .limit(limit > 0 ? limit : 100)
                .collect(Collectors.toList());
    }

    public Map<LogLevel, Long> getCountByLogLevel() {
        return indexedLogs.stream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));
    }

    public double getAverageResponseTimeMs() {
        return indexedLogs.stream()
                .mapToLong(LogEntry::responseTimeMs)
                .average()
                .orElse(0.0);
    }

    public List<LogEntry> getSlowestRequests(int topN) {
        return indexedLogs.stream()
                .sorted((a, b) -> Long.compare(b.responseTimeMs(), a.responseTimeMs()))
                .limit(topN > 0 ? topN : 10)
                .collect(Collectors.toList());
    }

    public int getTotalLogCount() {
        return indexedLogs.size();
    }

    public List<LogEntry> getAllLogs() {
        return List.copyOf(indexedLogs);
    }

    public void clear() {
        indexedLogs.clear();
    }
}
