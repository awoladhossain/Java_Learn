package com.example.httpserver.handlers;

import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;
import com.example.httpserver.metrics.MetricsCollector;
import com.example.httpserver.metrics.ServerMetricsDto;
import com.example.httpserver.routing.RouteHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetricsHandler implements RouteHandler {
    private final MetricsCollector metricsCollector;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MetricsHandler(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        ServerMetricsDto snapshot = metricsCollector.getSnapshot();
        String json = objectMapper.writeValueAsString(snapshot);
        return HttpResponse.json(json);
    }
}
