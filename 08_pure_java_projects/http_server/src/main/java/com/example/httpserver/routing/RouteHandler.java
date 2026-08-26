package com.example.httpserver.routing;

import com.example.httpserver.core.HttpRequest;
import com.example.httpserver.core.HttpResponse;

@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
