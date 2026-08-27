package com.example.banking.exception;

public class AccountLockedException extends BankingException {
    private final String accountId;

    public AccountLockedException(String accountId) {
        super("ACCOUNT_LOCKED", String.format("Account %s is currently locked or inactive", accountId));
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
