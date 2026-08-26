package com.example.httpserver.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestParserTest {

    @Test
    @DisplayName("Should parse standard GET request with query parameters and headers")
    void testParseGetRequest() throws Exception {
        String raw = """
            GET /api/logs?level=ERROR&limit=10 HTTP/1.1\r
            Host: localhost:8080\r
            User-Agent: JUnit5\r
            Accept: application/json\r
            \r
            """;

        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequestParser.parse(in);

        assertEquals(HttpMethod.GET, request.method());
        assertEquals("/api/logs", request.path());
        assertEquals("ERROR", request.getQueryParam("level"));
        assertEquals("10", request.getQueryParam("limit"));
        assertEquals("localhost:8080", request.getHeader("host"));
        assertEquals("JUnit5", request.getHeader("User-Agent"));
    }

    @Test
    @DisplayName("Should parse POST request with Content-Length and body payload")
    void testParsePostRequestWithBody() throws Exception {
        String body = "{\"log\": \"[2026-08-26T22:00:00Z] [ERROR] [10.0.0.1] POST /pay 500 100ms - Fail\"}";
        int length = body.getBytes(StandardCharsets.UTF_8).length;

        String raw = "POST /api/analyze HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + length + "\r\n" +
                "\r\n" +
                body;

        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequestParser.parse(in);

        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/api/analyze", request.path());
        assertEquals(body, request.body());
    }

    @Test
    @DisplayName("Should throw ParseException for malformed request line")
    void testMalformedRequest() {
        String raw = "INVALID_REQUEST_LINE\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));

        assertThrows(HttpRequestParser.ParseException.class, () -> HttpRequestParser.parse(in));
    }
}
