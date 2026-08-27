package com.example.banking;

import com.example.banking.config.DatabaseConfig;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.LedgerEntry;
import com.example.banking.model.OperationResult;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepositoryImpl;
import com.example.banking.repository.CustomerRepositoryImpl;
import com.example.banking.repository.LedgerRepositoryImpl;
import com.example.banking.repository.TransactionRepositoryImpl;
import com.example.banking.service.BankingService;
import com.example.banking.service.BankingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

public class BankingApplication {
    private static final Logger log = LoggerFactory.getLogger(BankingApplication.class);

    public static void main(String[] args) {
        log.info("=== Starting Core Banking & Transaction Engine Demo ===");

        DataSource ds = DatabaseConfig.getDataSource();
        DatabaseConfig.initializeDatabase(ds);

        BankingService bankingService = new BankingServiceImpl(
                ds,
                new CustomerRepositoryImpl(),
                new AccountRepositoryImpl(),
                new TransactionRepositoryImpl(),
                new LedgerRepositoryImpl()
        );

        try {
            // 1. Create Customers
            log.info("\n--- Creating Customers ---");
            Customer alice = bankingService.createCustomer("Alice Smith", "alice@example.com");
            Customer bob = bankingService.createCustomer("Bob Jones", "bob@example.com");
            log.info("Created Customer 1: {} (ID: {})", alice.name(), alice.id());
            log.info("Created Customer 2: {} (ID: {})", bob.name(), bob.id());

            // 2. Create Accounts
            log.info("\n--- Opening Accounts ---");
            Account accAlice = bankingService.createAccount(alice.id(), "ACC-ALICE-1001", new BigDecimal("1000.0000"), "USD");
            Account accBob = bankingService.createAccount(bob.id(), "ACC-BOB-2002", new BigDecimal("500.0000"), "USD");
            log.info("Alice's Account: {} | Balance: ${}", accAlice.accountNumber(), accAlice.balance());
            log.info("Bob's Account:   {} | Balance: ${}", accBob.accountNumber(), accBob.balance());

            // 3. Deposit Funds
            log.info("\n--- Depositing Funds ---");
            OperationResult<Transaction> depositRes = bankingService.deposit(accAlice.id(), new BigDecimal("250.0000"), "REF-DEP-01");
            if (depositRes instanceof OperationResult.Success<Transaction> s) {
                log.info("Deposit Successful: Tx ID={} | Ref={}", s.data().id(), s.data().referenceCode());
            }

            // 4. Money Transfer
            log.info("\n--- Transferring Funds ($400.00 from Alice to Bob) ---");
            OperationResult<Transaction> transferRes = bankingService.transfer(accAlice.id(), accBob.id(), new BigDecimal("400.0000"), "REF-TRF-01");
            if (transferRes instanceof OperationResult.Success<Transaction> s) {
                log.info("Transfer Successful: Tx ID={} | Ref={}", s.data().id(), s.data().referenceCode());
            }

            // 5. Attempt Invalid Withdrawal (Overdraft)
            log.info("\n--- Attempting Overdraft Withdrawal ($5000.00 from Bob) ---");
            OperationResult<Transaction> failRes = bankingService.withdraw(accBob.id(), new BigDecimal("5000.0000"), "REF-WTH-ERR");
            if (failRes instanceof OperationResult.Failure<Transaction> f) {
                log.info("Withdrawal Correctly Rejected: ErrorCode={} | Message={}", f.errorCode(), f.errorMessage());
            }

            // 6. Updated Account Balances & Audit Ledger
            log.info("\n--- Final Account Status & Ledger Audit ---");
            Account updatedAlice = bankingService.getAccount(accAlice.id()).orElseThrow();
            Account updatedBob = bankingService.getAccount(accBob.id()).orElseThrow();

            log.info("Alice Final Balance: ${}", updatedAlice.balance());
            log.info("Bob Final Balance:   ${}", updatedBob.balance());

            log.info("\n--- Alice's Double-Entry Audit Ledger ---");
            List<LedgerEntry> aliceLedger = bankingService.getLedgerEntries(accAlice.id());
            for (LedgerEntry entry : aliceLedger) {
                log.info(String.format("Ledger ID: %s | Type: %-6s | Amount: $%s | Balance After: $%s",
                        entry.id().substring(0, 8), entry.entryType(), entry.amount(), entry.balanceAfter()));
            }

            log.info("\n--- Bob's Double-Entry Audit Ledger ---");
            List<LedgerEntry> bobLedger = bankingService.getLedgerEntries(accBob.id());
            for (LedgerEntry entry : bobLedger) {
                log.info(String.format("Ledger ID: %s | Type: %-6s | Amount: $%s | Balance After: $%s",
                        entry.id().substring(0, 8), entry.entryType(), entry.amount(), entry.balanceAfter()));
            }

            log.info("\n=== Core Banking Demo Completed Successfully ===");
        } finally {
            DatabaseConfig.closeDataSource();
        }
    }
}
