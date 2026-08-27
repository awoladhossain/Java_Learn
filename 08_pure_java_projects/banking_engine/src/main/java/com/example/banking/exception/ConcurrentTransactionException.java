package com.example.banking.exception;

public class ConcurrentTransactionException extends BankingException {
    public ConcurrentTransactionException(String message) {
        super("CONCURRENT_TRANSACTION_CONFLICT", message);
    }

    public ConcurrentTransactionException(String message, Throwable cause) {
        super("CONCURRENT_TRANSACTION_CONFLICT", message, cause);
    }
}
