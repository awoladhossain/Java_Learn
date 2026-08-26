package com.example.httpserver.handlers;

import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;
import com.example.httpserver.log.LogAnalyzerService;
import com.example.httpserver.log.LogEntry;
import com.example.httpserver.log.LogLevel;
import com.example.httpserver.persistence.LogRepository;
import com.example.httpserver.routing.RouteHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeHandler implements RouteHandler {
    private final LogAnalyzerService logAnalyzerService;
    private final LogRepository logRepository;

    public AnalyzeHandler(LogAnalyzerService logAnalyzerService, LogRepository logRepository) {
        this.logAnalyzerService = logAnalyzerService;
        this.logRepository = logRepository;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        String body = request.body();
        if (body == null || body.isBlank()) {
            return HttpResponse.badRequest("Request body containing log text is required for log analysis.");
        }

        List<LogEntry> ingested = logAnalyzerService.ingestRawLogs(body);

        // Persist to DB if repository present
        if (logRepository != null && !ingested.isEmpty()) {
            try {
                logRepository.saveBatch(ingested);
            } catch (Exception e) {
                // Log warning and continue
            }
        }

        Map<LogLevel, Long> breakdown = logAnalyzerService.getCountByLogLevel();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"parsedCount\": ").append(ingested.size()).append(",\n");
        json.append("  \"totalIndexed\": ").append(logAnalyzerService.getTotalLogCount()).append(",\n");
        json.append("  \"averageResponseTimeMs\": ").append(logAnalyzerService.getAverageResponseTimeMs()).append(",\n");
        json.append("  \"levelBreakdown\": {\n");

        int i = 0;
        for (Map.Entry<LogLevel, Long> entry : breakdown.entrySet()) {
            json.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (++i < breakdown.size()) json.append(",");
            json.append("\n");
        }
        json.append("  }\n");
        json.append("}");

        return HttpResponse.json(json.toString());
    }
}
