package com.example.patterns.creational;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 🛠️ Creational Pattern: Builder (Fluent Interface)
 * 
 * Separates complex object construction from its representation, allowing step-by-step
 * construction of immutable objects with readable method chaining.
 */
public class BuilderPattern {

    public static class HttpRequest {
        private final String url;
        private final String method;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;
        private final String body;
        private final int connectTimeoutMs;
        private final int readTimeoutMs;

        private HttpRequest(Builder builder) {
            this.url = builder.url;
            this.method = builder.method;
            this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
            this.queryParams = Collections.unmodifiableMap(new HashMap<>(builder.queryParams));
            this.body = builder.body;
            this.connectTimeoutMs = builder.connectTimeoutMs;
            this.readTimeoutMs = builder.readTimeoutMs;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public Map<String, String> getHeaders() { return headers; }
        public Map<String, String> getQueryParams() { return queryParams; }
        public String getBody() { return body; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }

        @Override
        public String toString() {
            return String.format("HttpRequest[%s %s, headers=%d, connectTimeout=%dms, readTimeout=%dms]",
                    method, url, headers.size(), connectTimeoutMs, readTimeoutMs);
        }

        public static class Builder {
            private String url;
            private String method = "GET";
            private final Map<String, String> headers = new HashMap<>();
            private final Map<String, String> queryParams = new HashMap<>();
            private String body = "";
            private int connectTimeoutMs = 5000;
            private int readTimeoutMs = 10000;

            public Builder(String url) {
                if (url == null || url.isBlank()) {
                    throw new IllegalArgumentException("Target URL cannot be null or blank");
                }
                this.url = url;
            }

            public Builder method(String method) {
                if (method == null || method.isBlank()) {
                    throw new IllegalArgumentException("HTTP method cannot be blank");
                }
                this.method = method.toUpperCase();
                return this;
            }

            public Builder header(String key, String value) {
                this.headers.put(key, value);
                return this;
            }

            public Builder queryParam(String key, String value) {
                this.queryParams.put(key, value);
                return this;
            }

            public Builder body(String body) {
                this.body = body != null ? body : "";
                return this;
            }

            public Builder connectTimeoutMs(int timeoutMs) {
                if (timeoutMs < 0) throw new IllegalArgumentException("Timeout must be non-negative");
                this.connectTimeoutMs = timeoutMs;
                return this;
            }

            public Builder readTimeoutMs(int timeoutMs) {
                if (timeoutMs < 0) throw new IllegalArgumentException("Timeout must be non-negative");
                this.readTimeoutMs = timeoutMs;
                return this;
            }

            public HttpRequest build() {
                return new HttpRequest(this);
            }
        }
    }
}
