package com.example.patterns.behavioral;

/**
 * 🛠️ Behavioral Pattern: Chain of Responsibility
 * 
 * Avoids coupling the sender of a request to its receiver by giving more than one object a chance to handle the request.
 * Chains the receiving objects and passes the request along the chain until an object handles it or rejects it.
 * E.g., HTTP Request Security / Authentication / Sanitization Middleware Pipeline.
 */
public class ChainOfResponsibilityPattern {

    // HTTP Request Model
    public static class HttpRequest {
        private final String path;
        private final String authToken;
        private final String body;
        private final int clientRequestRate; // requests per second

        public HttpRequest(String path, String authToken, String body, int clientRequestRate) {
            this.path = path;
            this.authToken = authToken;
            this.body = body;
            this.clientRequestRate = clientRequestRate;
        }

        public String getPath() { return path; }
        public String getAuthToken() { return authToken; }
        public String getBody() { return body; }
        public int getClientRequestRate() { return clientRequestRate; }
    }

    // Abstract Handler
    public static abstract class RequestHandler {
        protected RequestHandler nextHandler;

        public RequestHandler setNext(RequestHandler nextHandler) {
            this.nextHandler = nextHandler;
            return nextHandler;
        }

        public abstract boolean handle(HttpRequest request);

        protected boolean passToNext(HttpRequest request) {
            if (nextHandler == null) {
                return true; // Chain reached end successfully
            }
            return nextHandler.handle(request);
        }
    }

    // Concrete Handler 1: Rate Limiting
    public static class RateLimitingHandler extends RequestHandler {
        private final int maxAllowedRps;

        public RateLimitingHandler(int maxAllowedRps) {
            this.maxAllowedRps = maxAllowedRps;
        }

        @Override
        public boolean handle(HttpRequest request) {
            if (request.getClientRequestRate() > maxAllowedRps) {
                System.out.printf("   [CHAIN BLOCKED] RateLimitingHandler: Request rate %d RPS exceeds limit %d RPS\n",
                        request.getClientRequestRate(), maxAllowedRps);
                return false;
            }
            return passToNext(request);
        }
    }

    // Concrete Handler 2: Authentication
    public static class AuthenticationHandler extends RequestHandler {
        @Override
        public boolean handle(HttpRequest request) {
            if (request.getAuthToken() == null || !request.getAuthToken().startsWith("Bearer valid-")) {
                System.out.println("   [CHAIN BLOCKED] AuthenticationHandler: Invalid or missing bearer token");
                return false;
            }
            return passToNext(request);
        }
    }

    // Concrete Handler 3: Payload Sanitization
    public static class SanitizationHandler extends RequestHandler {
        @Override
        public boolean handle(HttpRequest request) {
            if (request.getBody() != null && (request.getBody().contains("<script>") || request.getBody().contains("DROP TABLE"))) {
                System.out.println("   [CHAIN BLOCKED] SanitizationHandler: Malicious script/SQL payload detected");
                return false;
            }
            return passToNext(request);
        }
    }
}
