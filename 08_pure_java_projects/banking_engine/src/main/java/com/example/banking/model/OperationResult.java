package com.example.banking.model;

import com.example.banking.exception.BankingException;

public sealed interface OperationResult<T> permits OperationResult.Success, OperationResult.Failure {

    record Success<T>(T data, String message) implements OperationResult<T> {}

    record Failure<T>(BankingException exception, String errorCode, String errorMessage) implements OperationResult<T> {}

    static <T> Success<T> success(T data, String message) {
        return new Success<>(data, message);
    }

    static <T> Failure<T> failure(BankingException exception) {
        return new Failure<>(exception, exception.getErrorCode(), exception.getMessage());
    }

    static <T> Failure<T> failure(String errorCode, String message) {
        return new Failure<>(null, errorCode, message);
    }
}
