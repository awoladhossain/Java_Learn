package com.example.httpserver.log;

public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, UNKNOWN;

    public static LogLevel fromString(String level) {
        if (level == null) return UNKNOWN;
        try {
            return LogLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
