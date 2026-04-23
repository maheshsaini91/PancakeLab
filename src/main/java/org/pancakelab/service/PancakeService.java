package org.pancakelab.service;

import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.Order;
import org.pancakelab.model.Pancake;
import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PancakeService {
    private List<Order>         orders          = new ArrayList<>();
    private Set<UUID>           completedOrders = new HashSet<>();
    private Set<UUID>           preparedOrders  = new HashSet<>();
    private List<PancakeRecipe> pancakes        = new ArrayList<>();

    public Order createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.add(order);
        return order;
    }

    public void addPancake(UUID orderId, List<Ingredient> ingredients, int count) {
        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        for (int i = 0; i < count; i++) {
            Pancake pancake = new Pancake(ingredients);
            pancake.setOrderId(order.getId());
            pancakes.add(pancake);
            OrderLog.logAddPancake(order, pancake.description(), pancakes);
        }
    }

    public List<String> viewOrder(UUID orderId) {
        return pancakes.stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .map(PancakeRecipe::description)
                .toList();
    }

    public void removePancakes(String description, UUID orderId, int count) {
        AtomicInteger removed = new AtomicInteger(0);
        pancakes.removeIf(p -> p.getOrderId().equals(orderId) &&
                p.description().equals(description) &&
                removed.getAndIncrement() < count);

        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        OrderLog.logRemovePancakes(order, description, removed.get(), pancakes);
    }

    public void cancelOrder(UUID orderId) {
        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        pancakes.removeIf(p -> p.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));
        completedOrders.remove(orderId);
        preparedOrders.remove(orderId);
        OrderLog.logCancelOrder(order, pancakes);
    }

    public void completeOrder(UUID orderId) {
        completedOrders.add(orderId);
    }

    public Set<UUID> listCompletedOrders() {
        return completedOrders;
    }

    public void prepareOrder(UUID orderId) {
        preparedOrders.add(orderId);
        completedOrders.remove(orderId);
    }

    public Set<UUID> listPreparedOrders() {
        return preparedOrders;
    }

    public Object[] deliverOrder(UUID orderId) {
        if (!preparedOrders.contains(orderId)) return null;

        Order order = orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        List<String> pancakesToDeliver = viewOrder(orderId);
        OrderLog.logDeliverOrder(order, pancakes);

        pancakes.removeIf(p -> p.getOrderId().equals(orderId));
        orders.removeIf(o -> o.getId().equals(orderId));
        preparedOrders.remove(orderId);

        return new Object[]{order, pancakesToDeliver};
    }
}