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

import static org.pancakelab.service.UtilsService.validateUuid;

public class PancakeService {
    private final List<Order> orders = new CopyOnWriteArrayList<>();
    private final Set<UUID> completedOrders = ConcurrentHashMap.newKeySet();
    private final Set<UUID> preparedOrders = ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<PancakeRecipe>> pancakes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingPancakes = new ConcurrentHashMap<>();
    private final Map<UUID, List<Ingredient>> pendingIngredients = new ConcurrentHashMap<>();
    private final OrderLog orderLog = new OrderLog();

    public OrderInfo createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.add(order);
        pancakes.put(order.getId(), new CopyOnWriteArrayList<>());
        return new OrderInfo(order.getId(), order.getBuilding(), order.getRoom());
    }

    public UUID startPancake(UUID orderId) {
        Order order = findModifiableOrder(orderId);
        UUID pancakeId = UUID.randomUUID();
        pendingPancakes.put(pancakeId, order.getId());
        pendingIngredients.put(pancakeId, new CopyOnWriteArrayList<>());
        return pancakeId;
    }

    public void addIngredient(UUID orderId, UUID pancakeId, Ingredient ingredient) {
        validateUuid(orderId, "Order id");
        validateUuid(pancakeId, "Pancake id");
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient must not be null");
        }

        findModifiableOrder(orderId);
        validatePendingPancakeOwnership(orderId, pancakeId);

        List<Ingredient> ingredients = pendingIngredients.get(pancakeId);
        if (ingredients == null) {
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        }

        ingredients.add(ingredient);
    }

    public void finishPancake(UUID orderId, UUID pancakeId) {
        validateUuid(orderId, "Order id");
        validateUuid(pancakeId, "Pancake id");
        Order order = findModifiableOrder(orderId);
        validatePendingPancakeOwnership(orderId, pancakeId);

        List<Ingredient> ingredients = pendingIngredients.remove(pancakeId);
        pendingPancakes.remove(pancakeId);
        if (ingredients == null) {
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        }

        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Cannot finish a pancake with no ingredients");
        }

        Pancake pancake = new Pancake(ingredients);
        pancakes.get(orderId).add(pancake);
        orderLog.logAddPancake(order, pancake.description(), pancakes.get(orderId));
    }

    public List<String> viewOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        List<PancakeRecipe> orderPancakes = pancakes.get(orderId);
        if (orderPancakes == null) {
            return List.of();
        }

        return orderPancakes.stream()
                .map(PancakeRecipe::description)
                .toList();
    }

    public void removePancakes(String description, UUID orderId, int count) {
        validateUuid(orderId, "Order id");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }

        List<PancakeRecipe> orderPancakes = pancakes.get(orderId);
        if (orderPancakes == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        AtomicInteger removed = new AtomicInteger(0);
        orderPancakes.removeIf(p -> p.description().equals(description) &&
                removed.getAndIncrement() < count);

        Order order = findModifiableOrder(orderId);
        orderLog.logRemovePancakes(order, description, removed.get(), orderPancakes);
    }

    public void completeOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        findOrder(orderId);
        completedOrders.add(orderId);
    }

    public Set<UUID> listCompletedOrders() {
        return Set.copyOf(completedOrders);
    }

    public void prepareOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        if ( ! completedOrders.contains(orderId)) {
            throw new IllegalStateException("Order must be completed before preparing: " + orderId);
        }

        preparedOrders.add(orderId);
        completedOrders.remove(orderId);
    }

    public Set<UUID> listPreparedOrders() {
        return Set.copyOf(preparedOrders);
    }

    public DeliveryInfo deliverOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        if ( ! preparedOrders.contains(orderId)) {
            throw new IllegalStateException("Order must be prepared before delivering: " + orderId);
        }

        Order order = findOrder(orderId);
        List<String> pancakesToDeliver = viewOrder(orderId);
        orderLog.logDeliverOrder(order, pancakes.get(orderId));
        clearPendingPancakes(orderId);
        pancakes.remove(orderId);
        orders.removeIf(o -> o.getId().equals(orderId));
        completedOrders.remove(orderId);
        preparedOrders.remove(orderId);

        return new DeliveryInfo(order.getId(), order.getBuilding(), order.getRoom(), pancakesToDeliver);
    }

    public void cancelOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        Order order = findOrder(orderId);
        pancakes.remove(orderId);
        clearPendingPancakes(orderId);
        orders.removeIf(o -> o.getId().equals(orderId));
        completedOrders.remove(orderId);
        preparedOrders.remove(orderId);
        orderLog.logCancelOrder(order, List.of());
    }

    private Order findModifiableOrder(UUID orderId) {
        Order order = findOrder(orderId);
        if (completedOrders.contains(orderId) || preparedOrders.contains(orderId)) {
            throw new IllegalStateException("Cannot modify a completed or prepared order: " + orderId);
        }

        return order;
    }

    private void validatePendingPancakeOwnership(UUID orderId, UUID pancakeId) {
        UUID ownerOrderId = pendingPancakes.get(pancakeId);
        if (ownerOrderId == null) {
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        }

        if ( ! ownerOrderId.equals(orderId)) {
            throw new IllegalArgumentException("Pancake does not belong to order: " + pancakeId);
        }
    }

    private void clearPendingPancakes(UUID orderId) {
        List<UUID> pancakesToRemove = pendingPancakes.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(orderId))
                .map(Map.Entry::getKey)
                .toList();

        for (UUID pancakeId : pancakesToRemove) {
            pendingPancakes.remove(pancakeId);
            pendingIngredients.remove(pancakeId);
        }
    }

    private Order findOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        return orders.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
