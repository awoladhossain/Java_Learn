package com.example.banking.exception;

public class AccountNotFoundException extends BankingException {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super("ACCOUNT_NOT_FOUND", String.format("Account %s was not found", accountId));
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
