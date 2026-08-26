package com.example.httpserver.core;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record HttpRequest(
        HttpMethod method,
        String rawUri,
        String path,
        Map<String, String> queryParams,
        Map<String, String> headers,
        String body,
        String version
) {
    public HttpRequest {
        // Case-insensitive header lookup
        Map<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            caseInsensitiveHeaders.putAll(headers);
        }
        headers = Collections.unmodifiableMap(caseInsensitiveHeaders);
        queryParams = queryParams != null ? Collections.unmodifiableMap(queryParams) : Collections.emptyMap();
        body = body != null ? body : "";
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public String getQueryParamOrDefault(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }
}
