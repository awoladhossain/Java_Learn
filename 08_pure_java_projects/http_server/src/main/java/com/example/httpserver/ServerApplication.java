package com.example.httpserver;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class ServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port argument: {}, defaulting to 8080", args[0]);
            }
        }

        logger.info("Initializing Pure Java HTTP Web Server & Log Analyzer...");

        MetricsCollector metricsCollector = new MetricsCollector();
        LogAnalyzerService logAnalyzerService = new LogAnalyzerService(metricsCollector);

        LogRepository logRepository = null;
        try {
            DataSource ds = DataSourceFactory.getDataSource();
            logRepository = new LogRepository(ds);
            logRepository.initSchema();
            logger.info("HikariCP & H2 database schema initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize database persistence", e);
        }

        // Ingest baseline sample log data
        ingestInitialSampleData(logAnalyzerService, logRepository);

        RouteRegistry routeRegistry = new RouteRegistry();
        routeRegistry.get("/health", new HealthHandler());
        routeRegistry.get("/api/logs", new LogsHandler(logAnalyzerService, logRepository));
        routeRegistry.post("/api/analyze", new AnalyzeHandler(logAnalyzerService, logRepository));
        routeRegistry.get("/api/metrics", new MetricsHandler(metricsCollector));

        HttpServer server = new HttpServer(port, routeRegistry, metricsCollector);

        try {
            server.start();
            logger.info("===============================================================");
            logger.info("  🚀 Pure Java Server Running on http://localhost:{}", server.getPort());
            logger.info("  📌 Endpoints:");
            logger.info("     - GET  http://localhost:{}/health", server.getPort());
            logger.info("     - GET  http://localhost:{}/api/logs", server.getPort());
            logger.info("     - POST http://localhost:{}/api/analyze", server.getPort());
            logger.info("     - GET  http://localhost:{}/api/metrics", server.getPort());
            logger.info("===============================================================");
        } catch (Exception e) {
            logger.error("Failed to start HttpServer", e);
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Cleaning up resources...");
            server.stop();
            DataSourceFactory.close();
            logger.info("Server shutdown complete.");
        }));
    }

    private static void ingestInitialSampleData(LogAnalyzerService logAnalyzerService, LogRepository logRepository) {
        String sampleLogs = """
            [2026-08-26T22:00:01Z] [INFO] [192.168.1.15] GET /api/users 200 35ms - Fetched user list successfully
            [2026-08-26T22:00:05Z] [WARN] [192.168.1.22] POST /api/login 401 12ms - Invalid authentication credentials
            [2026-08-26T22:00:10Z] [ERROR] [192.168.1.50] POST /api/payment 500 120ms - Database transaction deadlock encountered
            [2026-08-26T22:00:15Z] [INFO] [192.168.1.15] GET /api/metrics 200 5ms - System metrics retrieved
            [2026-08-26T22:00:20Z] [FATAL] [10.0.0.1] POST /api/orders 503 500ms - Out of memory heap allocation failed
            """;
        var entries = logAnalyzerService.ingestRawLogs(sampleLogs);
        if (logRepository != null && !entries.isEmpty()) {
            try {
                logRepository.saveBatch(entries);
            } catch (Exception e) {
                logger.warn("Could not persist initial sample data to DB: {}", e.getMessage());
            }
        }
    }
}
