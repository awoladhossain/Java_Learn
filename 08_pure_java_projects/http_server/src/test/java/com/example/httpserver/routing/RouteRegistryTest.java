package com.example.httpserver.routing;

import com.example.httpserver.core.HttpMethod;
import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;
import com.example.httpserver.core.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouteRegistryTest {

    private RouteRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RouteRegistry();
        registry.get("/health", req -> HttpResponse.text("OK"));
        registry.post("/api/analyze", req -> HttpResponse.json("{\"analyzed\": true}"));
    }

    @Test
    @DisplayName("Should dispatch matching route handler successfully")
    void testDispatchSuccess() {
        HttpRequest req = new HttpRequest(HttpMethod.GET, "/health", "/health", Map.of(), Map.of(), "", "HTTP/1.1");
        HttpResponse res = registry.dispatch(req);

        assertEquals(HttpStatus.OK, res.getStatus());
        assertEquals("OK", new String(res.getBody()));
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed when method does not match path")
    void testMethodNotAllowed() {
        HttpRequest req = new HttpRequest(HttpMethod.POST, "/health", "/health", Map.of(), Map.of(), "", "HTTP/1.1");
        HttpResponse res = registry.dispatch(req);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, res.getStatus());
    }

    @Test
    @DisplayName("Should return 404 Not Found for unregistered routes")
    void testNotFound() {
        HttpRequest req = new HttpRequest(HttpMethod.GET, "/unknown", "/unknown", Map.of(), Map.of(), "", "HTTP/1.1");
        HttpResponse res = registry.dispatch(req);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatus());
    }
}
