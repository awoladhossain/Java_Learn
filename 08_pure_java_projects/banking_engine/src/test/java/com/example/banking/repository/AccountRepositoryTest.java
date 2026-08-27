package com.example.banking.repository;

import com.example.banking.config.DatabaseConfig;
import com.example.banking.model.Account;
import com.example.banking.model.AccountStatus;
import com.example.banking.model.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    private static DataSource dataSource;
    private CustomerRepository customerRepository;
    private AccountRepository accountRepository;

    @BeforeAll
    static void setUpAll() {
        dataSource = DatabaseConfig.getDataSource();
        DatabaseConfig.initializeDatabase(dataSource);
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseConfig.closeDataSource();
    }

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepositoryImpl();
        accountRepository = new AccountRepositoryImpl();
    }

    @Test
    @DisplayName("Should save, query, lock for update, and update balance for an account")
    void testAccountRepositoryCRUD() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String custId = UUID.randomUUID().toString();
            Customer customer = new Customer(custId, "Repo Test User", "repo.test@example.com", Instant.now());
            customerRepository.save(conn, customer);

            String accId = UUID.randomUUID().toString();
            Account account = new Account(accId, custId, "ACC-REPO-001", new BigDecimal("500.0000"), "USD", AccountStatus.ACTIVE, 1L, Instant.now());
            accountRepository.save(conn, account);

            Optional<Account> found = accountRepository.findById(conn, accId);
            assertTrue(found.isPresent());
            assertEquals("ACC-REPO-001", found.get().accountNumber());

            Optional<Account> locked = accountRepository.findByIdForUpdate(conn, accId);
            assertTrue(locked.isPresent());
            assertEquals(1L, locked.get().version());

            accountRepository.updateBalance(conn, accId, new BigDecimal("750.0000"), 1L);

            Optional<Account> updated = accountRepository.findById(conn, accId);
            assertTrue(updated.isPresent());
            assertEquals(new BigDecimal("750.0000"), updated.get().balance());
            assertEquals(2L, updated.get().version());

            conn.commit();
        }
    }
}
