package com.example.testing.domain;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    void updateStatus(String id, OrderStatus status);
}
