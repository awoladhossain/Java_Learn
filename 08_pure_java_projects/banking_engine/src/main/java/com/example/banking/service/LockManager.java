package com.example.banking.service;

import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class LockManager {
    private static final Logger log = LoggerFactory.getLogger(LockManager.class);

    public record LockedAccountPair(Account sourceAccount, Account targetAccount) {}

    public static LockedAccountPair acquireOrderedRowLocks(
            Connection conn,
            AccountRepository accountRepository,
            String sourceAccountId,
            String targetAccountId
    ) {
        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("Cannot lock the same account for transfer");
        }

        boolean sourceIsFirst = sourceAccountId.compareTo(targetAccountId) < 0;
        String firstId = sourceIsFirst ? sourceAccountId : targetAccountId;
        String secondId = sourceIsFirst ? targetAccountId : sourceAccountId;

        log.debug("Acquiring ordered row locks: First={}, Second={}", firstId, secondId);

        Account firstAccount = accountRepository.findByIdForUpdate(conn, firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));

        Account secondAccount = accountRepository.findByIdForUpdate(conn, secondId)
                .orElseThrow(() -> new AccountNotFoundException(secondId));

        Account source = sourceIsFirst ? firstAccount : secondAccount;
        Account target = sourceIsFirst ? secondAccount : firstAccount;

        return new LockedAccountPair(source, target);
    }
}
