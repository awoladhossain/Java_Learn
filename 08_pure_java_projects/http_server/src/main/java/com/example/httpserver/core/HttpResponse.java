package com.example.httpserver.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private HttpStatus status;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse(HttpStatus status) {
        this.status = status;
        setHeader("Server", "PureJava-VirtualThread-Server/1.0");
        setHeader("Connection", "close");
    }

    public static HttpResponse ok() {
        return new HttpResponse(HttpStatus.OK);
    }

    public static HttpResponse json(String jsonBody) {
        HttpResponse res = new HttpResponse(HttpStatus.OK);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody(jsonBody);
        return res;
    }

    public static HttpResponse json(HttpStatus status, String jsonBody) {
        HttpResponse res = new HttpResponse(status);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody(jsonBody);
        return res;
    }

    public static HttpResponse text(String textBody) {
        HttpResponse res = new HttpResponse(HttpStatus.OK);
        res.setHeader("Content-Type", "text/plain; charset=UTF-8");
        res.setBody(textBody);
        return res;
    }

    public static HttpResponse badRequest(String message) {
        HttpResponse res = new HttpResponse(HttpStatus.BAD_REQUEST);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody("{\"error\": \"" + escapeJson(message) + "\"}");
        return res;
    }

    public static HttpResponse notFound(String message) {
        HttpResponse res = new HttpResponse(HttpStatus.NOT_FOUND);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody("{\"error\": \"" + escapeJson(message) + "\"}");
        return res;
    }

    public static HttpResponse methodNotAllowed(String message) {
        HttpResponse res = new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody("{\"error\": \"" + escapeJson(message) + "\"}");
        return res;
    }

    public static HttpResponse serverError(String message) {
        HttpResponse res = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody("{\"error\": \"" + escapeJson(message) + "\"}");
        return res;
    }

    public HttpResponse setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpResponse setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpResponse setBody(String bodyText) {
        if (bodyText != null) {
            this.body = bodyText.getBytes(StandardCharsets.UTF_8);
        } else {
            this.body = new byte[0];
        }
        setHeader("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public HttpResponse setBody(byte[] bodyBytes) {
        this.body = bodyBytes != null ? bodyBytes : new byte[0];
        setHeader("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String statusLine = "HTTP/1.1 " + status.getFormattedStatusLine() + "\r\n";
        baos.write(statusLine.getBytes(StandardCharsets.UTF_8));

        // Auto set content-length if missing
        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(body.length));
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            baos.write(headerLine.getBytes(StandardCharsets.UTF_8));
        }
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        if (body.length > 0) {
            baos.write(body);
        }
        return baos.toByteArray();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
