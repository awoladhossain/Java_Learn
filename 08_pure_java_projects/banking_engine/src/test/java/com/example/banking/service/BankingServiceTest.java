package com.example.banking.service;

import com.example.banking.exception.AccountLockedException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.model.Account;
import com.example.banking.model.AccountStatus;
import com.example.banking.model.Customer;
import com.example.banking.model.OperationResult;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.LedgerRepository;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingServiceTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private LedgerRepository ledgerRepository;

    private BankingServiceImpl bankingService;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        bankingService = new BankingServiceImpl(
                dataSource, customerRepository, accountRepository, transactionRepository, ledgerRepository
        );
    }

    @Test
    @DisplayName("Should successfully deposit money into an active account")
    void deposit_Success() throws SQLException {
        String accountId = "acc-1";
        BigDecimal depositAmount = new BigDecimal("100.0000");
        Account existingAccount = new Account(
                accountId, "cust-1", "ACC100", new BigDecimal("500.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now()
        );

        when(accountRepository.findByIdForUpdate(eq(connection), eq(accountId)))
                .thenReturn(Optional.of(existingAccount));

        OperationResult<Transaction> result = bankingService.deposit(accountId, depositAmount, "REF-DEP-TEST");

        assertTrue(result instanceof OperationResult.Success);
        OperationResult.Success<Transaction> success = (OperationResult.Success<Transaction>) result;
        assertEquals(TransactionStatus.SUCCESS, success.data().status());
        assertEquals(depositAmount, success.data().amount());

        verify(accountRepository).updateBalance(eq(connection), eq(accountId), eq(new BigDecimal("600.0000")), eq(1L));
        verify(ledgerRepository).save(eq(connection), any());
        verify(connection).commit();
    }

    @Test
    @DisplayName("Should fail withdrawal when account has insufficient balance")
    void withdraw_InsufficientBalance() throws SQLException {
        String accountId = "acc-1";
        BigDecimal withdrawAmount = new BigDecimal("1000.0000");
        Account existingAccount = new Account(
                accountId, "cust-1", "ACC100", new BigDecimal("200.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now()
        );

        when(accountRepository.findByIdForUpdate(eq(connection), eq(accountId)))
                .thenReturn(Optional.of(existingAccount));

        OperationResult<Transaction> result = bankingService.withdraw(accountId, withdrawAmount, "REF-WTH-FAIL");

        assertTrue(result instanceof OperationResult.Failure);
        OperationResult.Failure<Transaction> failure = (OperationResult.Failure<Transaction>) result;
        assertEquals("INSUFFICIENT_FUNDS", failure.errorCode());
        assertTrue(failure.exception() instanceof InsufficientBalanceException);

        verify(connection).rollback();
    }

    @Test
    @DisplayName("Should successfully transfer money between accounts with double-entry ledger")
    void transfer_Success() throws SQLException {
        String sourceId = "acc-1";
        String targetId = "acc-2";
        BigDecimal transferAmount = new BigDecimal("300.0000");

        Account sourceAccount = new Account(sourceId, "c1", "A1", new BigDecimal("1000.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now());
        Account targetAccount = new Account(targetId, "c2", "A2", new BigDecimal("200.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now());

        when(accountRepository.findByIdForUpdate(eq(connection), eq(sourceId))).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(eq(connection), eq(targetId))).thenReturn(Optional.of(targetAccount));

        OperationResult<Transaction> result = bankingService.transfer(sourceId, targetId, transferAmount, "REF-TRF-TEST");

        assertTrue(result instanceof OperationResult.Success);
        verify(accountRepository).updateBalance(eq(connection), eq(sourceId), eq(new BigDecimal("700.0000")), eq(1L));
        verify(accountRepository).updateBalance(eq(connection), eq(targetId), eq(new BigDecimal("500.0000")), eq(1L));
        verify(ledgerRepository, times(2)).save(eq(connection), any());
        verify(connection).commit();
    }

    @Test
    @DisplayName("Should fail transfer when locked account is involved")
    void transfer_AccountLocked() throws SQLException {
        String sourceId = "acc-1";
        String targetId = "acc-2";

        Account sourceAccount = new Account(sourceId, "c1", "A1", new BigDecimal("1000.0000"), "USD", AccountStatus.LOCKED, 1L, Instant.now());
        Account targetAccount = new Account(targetId, "c2", "A2", new BigDecimal("200.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now());

        when(accountRepository.findByIdForUpdate(eq(connection), eq(sourceId))).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(eq(connection), eq(targetId))).thenReturn(Optional.of(targetAccount));

        OperationResult<Transaction> result = bankingService.transfer(sourceId, targetId, new BigDecimal("100.0000"), "REF-LOCKED");

        assertTrue(result instanceof OperationResult.Failure);
        OperationResult.Failure<Transaction> failure = (OperationResult.Failure<Transaction>) result;
        assertEquals("ACCOUNT_LOCKED", failure.errorCode());
        assertTrue(failure.exception() instanceof AccountLockedException);
        verify(connection).rollback();
    }
}
