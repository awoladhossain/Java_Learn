package com.example.httpserver.integration;

import com.example.httpserver.core.HttpServer;
import com.example.httpserver.handlers.AnalyzeHandler;
import com.example.httpserver.handlers.HealthHandler;
import com.example.httpserver.handlers.LogsHandler;
import com.example.httpserver.handlers.MetricsHandler;
import com.example.httpserver.log.LogAnalyzerService;
import com.example.httpserver.metrics.MetricsCollector;
import com.example.httpserver.persistence.DataSourceFactory;
import com.example.httpserver.persistence.LogRepository;
import com.example.httpserver.routing.RouteRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerIntegrationTest {

    private static HttpServer server;
    private static int port;
    private static HttpClient httpClient;

    @BeforeAll
    static void setUpAll() throws Exception {
        MetricsCollector metricsCollector = new MetricsCollector();
        LogAnalyzerService logAnalyzerService = new LogAnalyzerService(metricsCollector);

        DataSource ds = DataSourceFactory.getDataSource();
        LogRepository logRepository = new LogRepository(ds);
        logRepository.initSchema();

        RouteRegistry routeRegistry = new RouteRegistry();
        routeRegistry.get("/health", new HealthHandler());
        routeRegistry.get("/api/logs", new LogsHandler(logAnalyzerService, logRepository));
        routeRegistry.post("/api/analyze", new AnalyzeHandler(logAnalyzerService, logRepository));
        routeRegistry.get("/api/metrics", new MetricsHandler(metricsCollector));

        // Start on ephemeral port 0
        server = new HttpServer(0, routeRegistry, metricsCollector);
        server.start();
        port = server.getPort();

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void tearDownAll() {
        if (server != null) {
            server.stop();
        }
        DataSourceFactory.close();
    }

    @Test
    @DisplayName("GET /health should return 200 OK with JSON status")
    void testHealthEndpoint() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/health"))
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"status\": \"UP\""));
    }

    @Test
    @DisplayName("POST /api/analyze should ingest log data and return analysis summary")
    void testAnalyzeEndpoint() throws Exception {
        String logData = "[2026-08-26T22:00:00Z] [ERROR] [192.168.1.1] GET /api/data 500 80ms - Connection failed";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/analyze"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(logData))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"parsedCount\": 1"));
    }

    @Test
    @DisplayName("GET /api/logs should query indexed logs")
    void testGetLogsEndpoint() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/logs?level=ERROR"))
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().startsWith("["));
    }

    @Test
    @DisplayName("GET /api/metrics should return metrics JSON snapshot")
    void testMetricsEndpoint() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/metrics"))
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"totalRequests\""));
    }

    @Test
    @DisplayName("Should handle 50 concurrent requests cleanly via Virtual Threads")
    void testConcurrentRequests() throws InterruptedException {
        int count = 50;
        CountDownLatch latch = new CountDownLatch(count);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:" + port + "/health"))
                                .GET()
                                .build();
                        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                        assertEquals(200, res.statusCode());
                    } catch (Exception e) {
                        fail("Concurrent request failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }
    }
}
