package org.pancakelab.repository;

import org.pancakelab.enums.OrderStatus;
import org.pancakelab.model.Order;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(UUID orderId);
    Optional<OrderStatus> getStatus(UUID orderId);
    void delete(UUID orderId);
    void markCompleted(UUID orderId);
    void markPrepared(UUID orderId);
    void markDelivered(UUID orderId);
    boolean isCompleted(UUID orderId);
    boolean isPrepared(UUID orderId);
    Set<UUID> getCompletedOrders();
    Set<UUID> getPreparedOrders();
}
