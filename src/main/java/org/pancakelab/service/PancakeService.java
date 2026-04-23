package org.pancakelab.service;

import org.pancakelab.dto.DeliveryInfo;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Pancake;
import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PancakeService {
    private final List<Order>                    orders          = new CopyOnWriteArrayList<>();
    private final Set<UUID>                      completedOrders = ConcurrentHashMap.newKeySet();
    private final Set<UUID>                      preparedOrders  = ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<PancakeRecipe>> pancakes        = new ConcurrentHashMap<>();
    private final Map<UUID, List<Ingredient>>    pendingPancakes = new ConcurrentHashMap<>();
    private final OrderLog                       orderLog        = new OrderLog();

    public OrderInfo createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.add(order);
        pancakes.put(order.getId(), new CopyOnWriteArrayList<>());
        return new OrderInfo(order.getId(), order.getBuilding(), order.getRoom());
    }

    public UUID startPancake(UUID orderId) {
        findOrder(orderId);
        UUID pancakeId = UUID.randomUUID();
        pendingPancakes.put(pancakeId, new CopyOnWriteArrayList<>());
        return pancakeId;
    }

    public void addIngredient(UUID orderId, UUID pancakeId, Ingredient ingredient) {
        findOrder(orderId);
        if (!pendingPancakes.containsKey(pancakeId))
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        pendingPancakes.get(pancakeId).add(ingredient);
    }

    public void finishPancake(UUID orderId, UUID pancakeId) {
        Order order = findOrder(orderId);
        if (!pendingPancakes.containsKey(pancakeId))
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        List<Ingredient> ingredients = pendingPancakes.remove(pancakeId);
        if (ingredients.isEmpty())
            throw new IllegalStateException("Cannot finish a pancake with no ingredients");
        Pancake pancake = new Pancake(ingredients);
        pancakes.get(orderId).add(pancake);
        orderLog.logAddPancake(order, pancake.description(), pancakes.get(orderId));
    }

    public List<String> viewOrder(UUID orderId) {
        List<PancakeRecipe> orderPancakes = pancakes.get(orderId);
        if (orderPancakes == null) return List.of();
        return orderPancakes.stream()
                .map(PancakeRecipe::description)
                .toList();
    }

    public void removePancakes(String description, UUID orderId, int count) {
        List<PancakeRecipe> orderPancakes = pancakes.getOrDefault(orderId, List.of());
        AtomicInteger removed = new AtomicInteger(0);
        orderPancakes.removeIf(p -> p.description().equals(description) &&
                removed.getAndIncrement() < count);
        orderLog.logRemovePancakes(findOrder(orderId), description, removed.get(), orderPancakes);
    }

    public void cancelOrder(UUID orderId) {
        Order order = findOrder(orderId);
        pancakes.remove(orderId);
        orders.removeIf(o -> o.getId().equals(orderId));
        completedOrders.remove(orderId);
        preparedOrders.remove(orderId);
        orderLog.logCancelOrder(order, List.of());
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

    public DeliveryInfo deliverOrder(UUID orderId) {
        if (!preparedOrders.contains(orderId)) return null;
        Order order = findOrder(orderId);
        List<String> pancakesToDeliver = viewOrder(orderId);
        orderLog.logDeliverOrder(order, pancakes.get(orderId));
        pancakes.remove(orderId);
        orders.removeIf(o -> o.getId().equals(orderId));
        preparedOrders.remove(orderId);
        return new DeliveryInfo(order.getId(), order.getBuilding(), order.getRoom(), pancakesToDeliver);
    }

    private Order findOrder(UUID orderId) {
        return orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}