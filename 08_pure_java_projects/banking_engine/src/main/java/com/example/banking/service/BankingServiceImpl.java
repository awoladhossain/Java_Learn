package com.example.banking.service;

import com.example.banking.exception.AccountLockedException;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.BankingException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidTransactionException;
import com.example.banking.model.Account;
import com.example.banking.model.AccountStatus;
import com.example.banking.model.Customer;
import com.example.banking.model.LedgerEntry;
import com.example.banking.model.LedgerEntryType;
import com.example.banking.model.OperationResult;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.LedgerRepository;
import com.example.banking.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BankingServiceImpl implements BankingService {
    private static final Logger log = LoggerFactory.getLogger(BankingServiceImpl.class);

    private final DataSource dataSource;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public BankingServiceImpl(
            DataSource dataSource,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository
    ) {
        this.dataSource = dataSource;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public Customer createCustomer(String name, String email) {
        String id = UUID.randomUUID().toString();
        Customer customer = new Customer(id, name, email, Instant.now());
        try (Connection conn = dataSource.getConnection()) {
            return customerRepository.save(conn, customer);
        } catch (SQLException e) {
            log.error("Error creating customer", e);
            throw new RuntimeException("Error creating customer", e);
        }
    }

    @Override
    public Account createAccount(String customerId, String accountNumber, BigDecimal initialBalance, String currency) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Initial balance cannot be negative");
        }
        String id = UUID.randomUUID().toString();
        Account account = new Account(id, customerId, accountNumber, initialBalance, currency, AccountStatus.ACTIVE, 1L, Instant.now());
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Verify customer exists
                customerRepository.findById(conn, customerId)
                        .orElseThrow(() -> new BankingException("CUSTOMER_NOT_FOUND", "Customer not found: " + customerId));

                Account savedAccount = accountRepository.save(conn, account);

                if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
                    // Record initial deposit transaction & ledger entry
                    String txId = UUID.randomUUID().toString();
                    Transaction tx = new Transaction(
                            txId, null, id, initialBalance, TransactionType.DEPOSIT,
                            TransactionStatus.SUCCESS, "INIT_DEPOSIT", null, Instant.now()
                    );
                    transactionRepository.save(conn, tx);

                    LedgerEntry ledger = new LedgerEntry(
                            UUID.randomUUID().toString(), txId, id, LedgerEntryType.CREDIT,
                            initialBalance, initialBalance, Instant.now()
                    );
                    ledgerRepository.save(conn, ledger);
                }

                conn.commit();
                return savedAccount;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Error creating account", e);
            throw new RuntimeException("Error creating account", e);
        }
    }

    @Override
    public OperationResult<Transaction> deposit(String accountId, BigDecimal amount, String referenceCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return OperationResult.failure(new InvalidTransactionException("Deposit amount must be positive"));
        }

        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, null, accountId, amount, TransactionType.DEPOSIT,
                TransactionStatus.PENDING, referenceCode, null, Instant.now());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionRepository.save(conn, tx);

                Account account = accountRepository.findByIdForUpdate(conn, accountId)
                        .orElseThrow(() -> new AccountNotFoundException(accountId));

                if (account.status() != AccountStatus.ACTIVE) {
                    throw new AccountLockedException(accountId);
                }

                BigDecimal newBalance = account.balance().add(amount);
                accountRepository.updateBalance(conn, accountId, newBalance, account.version());

                LedgerEntry creditEntry = new LedgerEntry(
                        UUID.randomUUID().toString(), txId, accountId, LedgerEntryType.CREDIT,
                        amount, newBalance, Instant.now()
                );
                ledgerRepository.save(conn, creditEntry);

                transactionRepository.updateStatus(conn, txId, TransactionStatus.SUCCESS, null);
                conn.commit();

                Transaction completedTx = tx.withStatus(TransactionStatus.SUCCESS, null);
                return OperationResult.success(completedTx, "Deposit completed successfully");
            } catch (BankingException e) {
                conn.rollback();
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure(e);
            } catch (Exception e) {
                conn.rollback();
                log.error("Unhandled error during deposit", e);
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure("SYSTEM_ERROR", e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Database connection failure during deposit", e);
            return OperationResult.failure("DATABASE_ERROR", e.getMessage());
        }
    }

    @Override
    public OperationResult<Transaction> withdraw(String accountId, BigDecimal amount, String referenceCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return OperationResult.failure(new InvalidTransactionException("Withdrawal amount must be positive"));
        }

        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, accountId, null, amount, TransactionType.WITHDRAWAL,
                TransactionStatus.PENDING, referenceCode, null, Instant.now());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionRepository.save(conn, tx);

                Account account = accountRepository.findByIdForUpdate(conn, accountId)
                        .orElseThrow(() -> new AccountNotFoundException(accountId));

                if (account.status() != AccountStatus.ACTIVE) {
                    throw new AccountLockedException(accountId);
                }

                if (account.balance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException(accountId, amount, account.balance());
                }

                BigDecimal newBalance = account.balance().subtract(amount);
                accountRepository.updateBalance(conn, accountId, newBalance, account.version());

                LedgerEntry debitEntry = new LedgerEntry(
                        UUID.randomUUID().toString(), txId, accountId, LedgerEntryType.DEBIT,
                        amount, newBalance, Instant.now()
                );
                ledgerRepository.save(conn, debitEntry);

                transactionRepository.updateStatus(conn, txId, TransactionStatus.SUCCESS, null);
                conn.commit();

                Transaction completedTx = tx.withStatus(TransactionStatus.SUCCESS, null);
                return OperationResult.success(completedTx, "Withdrawal completed successfully");
            } catch (BankingException e) {
                conn.rollback();
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure(e);
            } catch (Exception e) {
                conn.rollback();
                log.error("Unhandled error during withdrawal", e);
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure("SYSTEM_ERROR", e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Database connection failure during withdrawal", e);
            return OperationResult.failure("DATABASE_ERROR", e.getMessage());
        }
    }

    @Override
    public OperationResult<Transaction> transfer(String sourceAccountId, String targetAccountId, BigDecimal amount, String referenceCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return OperationResult.failure(new InvalidTransactionException("Transfer amount must be positive"));
        }
        if (sourceAccountId.equals(targetAccountId)) {
            return OperationResult.failure(new InvalidTransactionException("Source and target account cannot be identical"));
        }

        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, sourceAccountId, targetAccountId, amount, TransactionType.TRANSFER,
                TransactionStatus.PENDING, referenceCode, null, Instant.now());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionRepository.save(conn, tx);

                // Deadlock Prevention: Acquire row locks in deterministic total order of Account IDs
                LockManager.LockedAccountPair locks = LockManager.acquireOrderedRowLocks(
                        conn, accountRepository, sourceAccountId, targetAccountId
                );

                Account source = locks.sourceAccount();
                Account target = locks.targetAccount();

                if (source.status() != AccountStatus.ACTIVE) {
                    throw new AccountLockedException(sourceAccountId);
                }
                if (target.status() != AccountStatus.ACTIVE) {
                    throw new AccountLockedException(targetAccountId);
                }

                if (!source.currency().equalsIgnoreCase(target.currency())) {
                    throw new InvalidTransactionException(
                            String.format("Currency mismatch: %s vs %s", source.currency(), target.currency()));
                }

                if (source.balance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException(sourceAccountId, amount, source.balance());
                }

                BigDecimal sourceNewBalance = source.balance().subtract(amount);
                BigDecimal targetNewBalance = target.balance().add(amount);

                accountRepository.updateBalance(conn, sourceAccountId, sourceNewBalance, source.version());
                accountRepository.updateBalance(conn, targetAccountId, targetNewBalance, target.version());

                // Double-Entry Ledger Bookkeeping
                LedgerEntry debitEntry = new LedgerEntry(
                        UUID.randomUUID().toString(), txId, sourceAccountId, LedgerEntryType.DEBIT,
                        amount, sourceNewBalance, Instant.now()
                );
                LedgerEntry creditEntry = new LedgerEntry(
                        UUID.randomUUID().toString(), txId, targetAccountId, LedgerEntryType.CREDIT,
                        amount, targetNewBalance, Instant.now()
                );

                ledgerRepository.save(conn, debitEntry);
                ledgerRepository.save(conn, creditEntry);

                transactionRepository.updateStatus(conn, txId, TransactionStatus.SUCCESS, null);
                conn.commit();

                Transaction completedTx = tx.withStatus(TransactionStatus.SUCCESS, null);
                return OperationResult.success(completedTx, "Transfer completed successfully");
            } catch (BankingException e) {
                conn.rollback();
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure(e);
            } catch (Exception e) {
                conn.rollback();
                log.error("Unhandled error during transfer", e);
                transactionRepository.updateStatus(conn, txId, TransactionStatus.FAILED, e.getMessage());
                return OperationResult.failure("SYSTEM_ERROR", e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Database connection failure during transfer", e);
            return OperationResult.failure("DATABASE_ERROR", e.getMessage());
        }
    }

    @Override
    public Optional<Account> getAccount(String accountId) {
        try (Connection conn = dataSource.getConnection()) {
            return accountRepository.findById(conn, accountId);
        } catch (SQLException e) {
            log.error("Error fetching account: {}", accountId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<LedgerEntry> getLedgerEntries(String accountId) {
        try (Connection conn = dataSource.getConnection()) {
            return ledgerRepository.findByAccountId(conn, accountId);
        } catch (SQLException e) {
            log.error("Error fetching ledger entries for account: {}", accountId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountId) {
        try (Connection conn = dataSource.getConnection()) {
            return transactionRepository.findByAccountId(conn, accountId);
        } catch (SQLException e) {
            log.error("Error fetching transactions for account: {}", accountId, e);
            return Collections.emptyList();
        }
    }
}
