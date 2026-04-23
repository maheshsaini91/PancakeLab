package org.pancakelab.repository.impl;

import org.pancakelab.enums.OrderStatus;
import org.pancakelab.model.Order;
import org.pancakelab.repository.OrderRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();
    private final Map<UUID, OrderStatus> statuses = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
        statuses.put(order.getId(), OrderStatus.OPEN);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public Optional<OrderStatus> getStatus(UUID orderId) {
        return Optional.ofNullable(statuses.get(orderId));
    }

    @Override
    public void delete(UUID orderId) {
        orders.remove(orderId);
        statuses.remove(orderId);
    }

    @Override
    public void markCompleted(UUID orderId) {
        transition(orderId, OrderStatus.OPEN, OrderStatus.COMPLETED);
    }

    @Override
    public void markPrepared(UUID orderId) {
        transition(orderId, OrderStatus.COMPLETED, OrderStatus.PREPARED);
    }

    @Override
    public void markDelivered(UUID orderId) {
        transition(orderId, OrderStatus.PREPARED, OrderStatus.DELIVERED);
    }

    @Override
    public boolean isCompleted(UUID orderId) {
        return statuses.get(orderId) == OrderStatus.COMPLETED;
    }

    @Override
    public boolean isPrepared(UUID orderId) {
        return statuses.get(orderId) == OrderStatus.PREPARED;
    }

    @Override
    public Set<UUID> getCompletedOrders() {
        return statuses.entrySet().stream()
                .filter(e -> e.getValue() == OrderStatus.COMPLETED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<UUID> getPreparedOrders() {
        return statuses.entrySet().stream()
                .filter(e -> e.getValue() == OrderStatus.PREPARED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void transition(UUID orderId, OrderStatus expected, OrderStatus target) {
        OrderStatus current = statuses.get(orderId);
        if (current == null)
            throw new IllegalArgumentException("Order not found: " + orderId);
        if (current != expected)
            throw new IllegalStateException("Invalid status transition for order %s: %s -> %s"
                    .formatted(orderId, current, target));
        statuses.put(orderId, target);
    }
}
