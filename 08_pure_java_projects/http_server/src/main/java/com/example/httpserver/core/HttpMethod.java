package com.example.httpserver.core;

public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, UNKNOWN;

    public static HttpMethod fromString(String method) {
        if (method == null) return UNKNOWN;
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
