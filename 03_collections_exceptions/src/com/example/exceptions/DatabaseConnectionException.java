package com.example.exceptions;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete Checked Custom Exception representing transient database connectivity failures.
 * Extends java.lang.Exception (Checked Exception - compiler forces explicit handling or declaration).
 */
public class DatabaseConnectionException extends Exception {

    private final ErrorCode errorCode;
    private final String correlationId;
    private final Instant timestamp;
    private final String dbUrl;

    public DatabaseConnectionException(String dbUrl, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.DATABASE_CONNECTION_ERROR;
        this.correlationId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now();
        this.dbUrl = dbUrl;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    @Override
    public String getMessage() {
        return String.format("[%s][TraceID: %s][DB: %s] %s (Retryable: %s)",
                errorCode.getCode(), correlationId, dbUrl, super.getMessage(), errorCode.isRetryable());
    }
}
