package com.example.exceptions;

import java.time.Instant;
import java.util.UUID;

/**
 * Base Domain Runtime Exception for production application architecture.
 * 
 * Provides:
 * 1. Structured Error Code (ErrorCode enum).
 * 2. Correlation ID for distributed tracing (MDC / OpenTelemetry context).
 * 3. Timestamp of exception occurrence.
 * 4. Support for Exception Chaining via super(message, cause).
 */
public abstract class BaseDomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String correlationId;
    private final Instant timestamp;

    public BaseDomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now();
    }

    public BaseDomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now();
    }

    public BaseDomainException(ErrorCode errorCode, String message, String correlationId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now();
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

    @Override
    public String getMessage() {
        return String.format("[%s][TraceID: %s][At: %s] %s | Code Details: %s",
                errorCode.getCode(), correlationId, timestamp, super.getMessage(), errorCode);
    }
}
