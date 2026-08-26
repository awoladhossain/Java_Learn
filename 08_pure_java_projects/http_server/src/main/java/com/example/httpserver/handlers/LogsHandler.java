package com.example.httpserver.handlers;

import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;
import com.example.httpserver.log.LogAnalyzerService;
import com.example.httpserver.log.LogEntry;
import com.example.httpserver.log.LogLevel;
import com.example.httpserver.persistence.LogRepository;
import com.example.httpserver.routing.RouteHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class LogsHandler implements RouteHandler {
    private final LogAnalyzerService logAnalyzerService;
    private final LogRepository logRepository;
    private final ObjectMapper objectMapper;

    public LogsHandler(LogAnalyzerService logAnalyzerService, LogRepository logRepository) {
        this.logAnalyzerService = logAnalyzerService;
        this.logRepository = logRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        String levelParam = request.getQueryParam("level");
        String minStatusParam = request.getQueryParam("minStatus");
        String keywordParam = request.getQueryParam("keyword");
        String limitParam = request.getQueryParamOrDefault("limit", "50");
        String sourceParam = request.getQueryParamOrDefault("source", "memory");

        int limit = parseInteger(limitParam, 50);

        List<LogEntry> logs;
        if ("db".equalsIgnoreCase(sourceParam) && logRepository != null) {
            logs = logRepository.findAll(limit);
        } else {
            LogLevel level = levelParam != null ? LogLevel.fromString(levelParam) : null;
            Integer minStatus = minStatusParam != null ? parseInteger(minStatusParam, 0) : null;
            logs = logAnalyzerService.queryLogs(level, minStatus, keywordParam, limit);
        }

        String jsonResult = objectMapper.writeValueAsString(logs);
        return HttpResponse.json(jsonResult);
    }

    private int parseInteger(String val, int defaultVal) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
