package com.example.httpserver.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static HttpRequest parse(InputStream inputStream) throws IOException, ParseException {
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isBlank()) {
            throw new ParseException("Empty HTTP request line");
        }

        String[] requestLineParts = requestLine.split(" ");
        if (requestLineParts.length < 3) {
            throw new ParseException("Malformed HTTP request line: " + requestLine);
        }

        HttpMethod method = HttpMethod.fromString(requestLineParts[0]);
        String rawUri = requestLineParts[1];
        String version = requestLineParts[2];

        // Separate path and query string
        String path = rawUri;
        Map<String, String> queryParams = new HashMap<>();

        int queryIndex = rawUri.indexOf('?');
        if (queryIndex != -1) {
            path = rawUri.substring(0, queryIndex);
            String queryString = rawUri.substring(queryIndex + 1);
            parseQueryParams(queryString, queryParams);
        }
        path = decodeUrl(path);

        // Parse HTTP Headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = readLine(inputStream)) != null && !headerLine.isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex != -1) {
                String headerName = headerLine.substring(0, colonIndex).trim();
                String headerValue = headerLine.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }

        // Parse Request Body if Content-Length is set
        String body = "";
        String contentLengthStr = getCaseInsensitive(headers, "Content-Length");
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr.trim());
                if (contentLength > 0) {
                    byte[] bodyBytes = inputStream.readNBytes(contentLength);
                    if (bodyBytes.length < contentLength) {
                        throw new ParseException("Incomplete request body: read " + bodyBytes.length + " of " + contentLength + " bytes");
                    }
                    body = new String(bodyBytes, StandardCharsets.UTF_8);
                }
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid Content-Length: " + contentLengthStr, e);
            }
        }

        return new HttpRequest(method, rawUri, path, queryParams, headers, body, version);
    }

    private static void parseQueryParams(String queryString, Map<String, String> queryParams) {
        if (queryString == null || queryString.isBlank()) return;
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex != -1) {
                String key = decodeUrl(pair.substring(0, eqIndex));
                String value = decodeUrl(pair.substring(eqIndex + 1));
                queryParams.put(key, value);
            } else if (!pair.isBlank()) {
                queryParams.put(decodeUrl(pair), "");
            }
        }
    }

    private static String decodeUrl(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static String getCaseInsensitive(Map<String, String> map, String key) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        boolean seenCr = false;

        while ((b = inputStream.read()) != -1) {
            if (b == '\r') {
                seenCr = true;
                continue;
            }
            if (b == '\n') {
                break;
            }
            if (seenCr) {
                baos.write('\r');
                seenCr = false;
            }
            baos.write(b);
        }

        if (b == -1 && baos.size() == 0 && !seenCr) {
            return null;
        }

        return baos.toString(StandardCharsets.UTF_8);
    }
}
