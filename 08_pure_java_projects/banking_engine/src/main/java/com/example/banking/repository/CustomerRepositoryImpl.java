package com.example.banking.repository;

import com.example.banking.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public Customer save(Connection conn, Customer customer) {
        String sql = "INSERT INTO customers (id, name, email, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.id());
            pstmt.setString(2, customer.name());
            pstmt.setString(3, customer.email());
            pstmt.setTimestamp(4, Timestamp.from(customer.createdAt()));
            pstmt.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer: " + customer.id(), e);
        }
    }

    @Override
    public Optional<Customer> findById(Connection conn, String id) {
        String sql = "SELECT id, name, email, created_at FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(Connection conn, String email) {
        String sql = "SELECT id, name, email, created_at FROM customers WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by email: " + email, e);
        }
        return Optional.empty();
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
