package com.example.banking.service;

import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.LedgerEntry;
import com.example.banking.model.OperationResult;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BankingService {
    Customer createCustomer(String name, String email);
    Account createAccount(String customerId, String accountNumber, BigDecimal initialBalance, String currency);
    OperationResult<Transaction> deposit(String accountId, BigDecimal amount, String referenceCode);
    OperationResult<Transaction> withdraw(String accountId, BigDecimal amount, String referenceCode);
    OperationResult<Transaction> transfer(String sourceAccountId, String targetAccountId, BigDecimal amount, String referenceCode);

    Optional<Account> getAccount(String accountId);
    List<LedgerEntry> getLedgerEntries(String accountId);
    List<Transaction> getTransactionHistory(String accountId);
}
