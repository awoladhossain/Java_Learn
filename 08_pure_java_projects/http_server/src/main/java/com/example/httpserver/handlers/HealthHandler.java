package com.example.httpserver.handlers;

import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;
import com.example.httpserver.routing.RouteHandler;

import java.time.Instant;

public class HealthHandler implements RouteHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        String json = """
            {
                "status": "UP",
                "timestamp": "%s",
                "engine": "Pure Java Virtual Threads",
                "javaVersion": "%s"
            }
            """.formatted(Instant.now().toString(), System.getProperty("java.version"));

        return HttpResponse.json(json);
    }
}
