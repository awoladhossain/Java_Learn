package com.example.exceptions;

/**
 * Custom Error Code Enum for Domain Exceptions.
 * 
 * Demonstrates structured error classification for backend services and SRE observability.
 * Each error code maps to:
 * - A unique string code (e.g., "ERR_RES_404")
 * - An HTTP status code equivalent
 * - An Error Category (TRANSIENT vs PERMANENT vs FATAL)
 * - A human-readable description template
 */
public enum ErrorCode {

    RESOURCE_NOT_FOUND("ERR_RES_404", 404, ErrorCategory.PERMANENT, "Requested resource was not found in system"),
    DATABASE_CONNECTION_ERROR("ERR_DB_503", 503, ErrorCategory.TRANSIENT, "Failed to establish or maintain database connection"),
    RATE_LIMIT_EXCEEDED("ERR_RATELIMIT_429", 429, ErrorCategory.TRANSIENT, "API request rate limit exceeded"),
    UNAUTHORIZED_ACCESS("ERR_AUTH_401", 401, ErrorCategory.PERMANENT, "Client unauthorized to execute requested action"),
    INTERNAL_SYSTEM_ERROR("ERR_SYS_500", 500, ErrorCategory.FATAL, "Unrecoverable internal system error occurred");

    public enum ErrorCategory {
        /** Transient errors can be retried using exponential backoff */
        TRANSIENT,
        /** Permanent errors will fail consistently unless client input or configuration changes */
        PERMANENT,
        /** Fatal errors indicate JVM or infrastructure instability needing immediate alert */
        FATAL
    }

    private final String code;
    private final int httpStatus;
    private final ErrorCategory category;
    private final String description;

    ErrorCode(String code, int httpStatus, ErrorCategory category, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.category = category;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRetryable() {
        return category == ErrorCategory.TRANSIENT;
    }

    @Override
    public String toString() {
        return String.format("[%s | HTTP %d | %s] %s", code, httpStatus, category, description);
    }
}
