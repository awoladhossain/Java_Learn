package com.example.banking.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BankingException {
    private final String accountId;
    private final BigDecimal requestedAmount;
    private final BigDecimal currentBalance;

    public InsufficientBalanceException(String accountId, BigDecimal requestedAmount, BigDecimal currentBalance) {
        super("INSUFFICIENT_FUNDS",
                String.format("Account %s has insufficient funds. Balance: %s, Requested: %s",
                        accountId, currentBalance, requestedAmount));
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.currentBalance = currentBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
}
