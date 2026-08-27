package com.example.banking.service;

import com.example.banking.config.DatabaseConfig;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.OperationResult;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepositoryImpl;
import com.example.banking.repository.CustomerRepositoryImpl;
import com.example.banking.repository.LedgerRepositoryImpl;
import com.example.banking.repository.TransactionRepositoryImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentTransferTest {

    private static DataSource dataSource;
    private static BankingService bankingService;

    @BeforeAll
    static void setUpAll() {
        dataSource = DatabaseConfig.getDataSource();
        DatabaseConfig.initializeDatabase(dataSource);

        bankingService = new BankingServiceImpl(
                dataSource,
                new CustomerRepositoryImpl(),
                new AccountRepositoryImpl(),
                new TransactionRepositoryImpl(),
                new LedgerRepositoryImpl()
        );
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseConfig.closeDataSource();
    }

    @Test
    @DisplayName("High Concurrency Bidirectional Transfer Test - Zero Deadlocks & Financial Conservation")
    void testConcurrentBidirectionalTransfers() throws InterruptedException {
        Customer alice = bankingService.createCustomer("Alice Concurrency", "alice.conc@example.com");
        Customer bob = bankingService.createCustomer("Bob Concurrency", "bob.conc@example.com");

        BigDecimal initialAliceBalance = new BigDecimal("10000.0000");
        BigDecimal initialBobBalance = new BigDecimal("10000.0000");
        BigDecimal totalInitial = initialAliceBalance.add(initialBobBalance);

        Account accAlice = bankingService.createAccount(alice.id(), "CONC-ACC-A", initialAliceBalance, "USD");
        Account accBob = bankingService.createAccount(bob.id(), "CONC-ACC-B", initialBobBalance, "USD");

        int numberOfThreads = 20;
        int transfersPerThread = 10;
        BigDecimal transferAmount = new BigDecimal("50.0000");

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final boolean aliceToBob = (i % 2 == 0);
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for sync start line signal
                    for (int j = 0; j < transfersPerThread; j++) {
                        OperationResult<Transaction> result;
                        if (aliceToBob) {
                            result = bankingService.transfer(accAlice.id(), accBob.id(), transferAmount, "TRF-A2B");
                        } else {
                            result = bankingService.transfer(accBob.id(), accAlice.id(), transferAmount, "TRF-B2A");
                        }

                        if (result instanceof OperationResult.Success) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();

        boolean completedInTime = completionLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(completedInTime, "Concurrent transfers timed out - possible deadlock!");
        assertEquals(0, failureCount.get(), "All concurrent transfers should succeed without errors");
        assertEquals(numberOfThreads * transfersPerThread, successCount.get());

        // Verify Financial Balance Invariant Conservation (Sum of A + B must equal initial $20,000.00)
        Account finalAlice = bankingService.getAccount(accAlice.id()).orElseThrow();
        Account finalBob = bankingService.getAccount(accBob.id()).orElseThrow();

        BigDecimal totalFinal = finalAlice.balance().add(finalBob.balance());

        assertEquals(0, totalInitial.compareTo(totalFinal),
                String.format("Financial conservation violated! Initial Total=%s, Final Total=%s", totalInitial, totalFinal));

        assertEquals(0, initialAliceBalance.compareTo(finalAlice.balance()),
                "Since equal number of A->B and B->A transfers occurred, Alice balance should be unchanged");
        assertEquals(0, initialBobBalance.compareTo(finalBob.balance()),
                "Since equal number of A->B and B->A transfers occurred, Bob balance should be unchanged");
    }
}
