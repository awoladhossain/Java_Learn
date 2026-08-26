package com.example.httpserver.routing;

import com.example.httpserver.core.HttpMethod;
import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RouteRegistry {

    public record RouteKey(HttpMethod method, String path) {}

    private final Map<RouteKey, RouteHandler> routes = new ConcurrentHashMap<>();
    private final Map<String, Boolean> registeredPaths = new ConcurrentHashMap<>();

    public RouteRegistry register(HttpMethod method, String path, RouteHandler handler) {
        Objects.requireNonNull(method, "Method cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");

        String normalizedPath = normalizePath(path);
        routes.put(new RouteKey(method, normalizedPath), handler);
        registeredPaths.put(normalizedPath, true);
        return this;
    }

    public RouteRegistry get(String path, RouteHandler handler) {
        return register(HttpMethod.GET, path, handler);
    }

    public RouteRegistry post(String path, RouteHandler handler) {
        return register(HttpMethod.POST, path, handler);
    }

    public RouteRegistry put(String path, RouteHandler handler) {
        return register(HttpMethod.PUT, path, handler);
    }

    public RouteRegistry delete(String path, RouteHandler handler) {
        return register(HttpMethod.DELETE, path, handler);
    }

    public HttpResponse dispatch(HttpRequest request) {
        String normalizedPath = normalizePath(request.path());
        RouteKey key = new RouteKey(request.method(), normalizedPath);

        RouteHandler handler = routes.get(key);
        if (handler != null) {
            try {
                return handler.handle(request);
            } catch (Exception e) {
                return HttpResponse.serverError("Internal Server Error: " + e.getMessage());
            }
        }

        // Check if path exists under a different method
        if (registeredPaths.containsKey(normalizedPath)) {
            return HttpResponse.methodNotAllowed("Method " + request.method() + " not allowed for path " + normalizedPath);
        }

        return HttpResponse.notFound("No handler registered for path: " + request.path());
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public int getRouteCount() {
        return routes.size();
    }
}
