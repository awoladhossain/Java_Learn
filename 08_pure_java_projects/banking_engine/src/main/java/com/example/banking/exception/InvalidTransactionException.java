package com.example.banking.exception;

public class InvalidTransactionException extends BankingException {
    public InvalidTransactionException(String message) {
        super("INVALID_TRANSACTION", message);
    }
}
