package com.example.httpserver.core;

import com.example.httpserver.metrics.MetricsCollector;
import com.example.httpserver.routing.RouteRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final int requestedPort;
    private int actualPort;
    private final RouteRegistry routeRegistry;
    private final MetricsCollector metricsCollector;

    private ServerSocket serverSocket;
    private ExecutorService virtualThreadExecutor;
    private volatile boolean running = false;

    public HttpServer(int port, RouteRegistry routeRegistry, MetricsCollector metricsCollector) {
        this.requestedPort = port;
        this.routeRegistry = routeRegistry;
        this.metricsCollector = metricsCollector;
    }

    public synchronized void start() throws IOException {
        if (running) return;

        serverSocket = new ServerSocket(requestedPort);
        actualPort = serverSocket.getLocalPort();
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        running = true;

        logger.info("HttpServer starting on port {} using Java 21 Virtual Threads", actualPort);

        // Run accept loop in a dedicated background virtual thread
        Thread.ofVirtual().name("http-server-acceptor").start(this::acceptLoop);
    }

    private void acceptLoop() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                virtualThreadExecutor.submit(() -> handleClient(clientSocket));
            } catch (SocketException e) {
                if (!running) {
                    logger.info("ServerSocket closed during shutdown.");
                } else {
                    logger.error("SocketException in accept loop: {}", e.getMessage());
                }
            } catch (IOException e) {
                if (running) {
                    logger.error("IOException in server accept loop", e);
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        long startTime = System.nanoTime();
        String routePath = "/";
        int statusCode = 500;
        long responseBytesLength = 0;

        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            socket.setSoTimeout(5000);

            HttpRequest request;
            try {
                request = HttpRequestParser.parse(in);
                routePath = request.path();
            } catch (HttpRequestParser.ParseException e) {
                logger.warn("Bad Request received: {}", e.getMessage());
                HttpResponse badRes = HttpResponse.badRequest(e.getMessage());
                statusCode = badRes.getStatus().getCode();
                byte[] raw = badRes.getBytes();
                responseBytesLength = raw.length;
                out.write(raw);
                out.flush();
                return;
            }

            HttpResponse response = routeRegistry.dispatch(request);
            statusCode = response.getStatus().getCode();
            byte[] responseBytes = response.getBytes();
            responseBytesLength = responseBytes.length;

            out.write(responseBytes);
            out.flush();

        } catch (IOException e) {
            logger.debug("Connection error or client disconnect: {}", e.getMessage());
        } finally {
            long latencyNs = System.nanoTime() - startTime;
            if (metricsCollector != null) {
                metricsCollector.recordRequest(routePath, statusCode, responseBytesLength, latencyNs);
            }
        }
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        logger.info("Stopping HttpServer on port {}", actualPort);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing ServerSocket", e);
        }

        if (virtualThreadExecutor != null) {
            virtualThreadExecutor.shutdown();
            try {
                if (!virtualThreadExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    virtualThreadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                virtualThreadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("HttpServer stopped.");
    }

    public int getPort() {
        return actualPort;
    }

    public boolean isRunning() {
        return running;
    }
}
