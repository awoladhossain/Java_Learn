package com.example.banking.repository;

import com.example.banking.model.Customer;

import java.sql.Connection;
import java.util.Optional;

public interface CustomerRepository {
    Customer save(Connection conn, Customer customer);
    Optional<Customer> findById(Connection conn, String id);
    Optional<Customer> findByEmail(Connection conn, String email);
}
