package org.pancakelab.service;

import org.pancakelab.dto.DeliveryInfo;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Pancake;
import org.pancakelab.model.pancakes.PancakeRecipe;
import org.pancakelab.repository.impl.InMemoryOrderRepository;
import org.pancakelab.repository.impl.InMemoryPancakeRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.repository.PancakeRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PancakeService {
    private final OrderRepository orderRepository;
    private final PancakeRepository pancakeRepository;
    private final OrderLog orderLog;

    public PancakeService() {
        this(new InMemoryOrderRepository(), new InMemoryPancakeRepository(), new OrderLog());
    }

    public PancakeService(OrderRepository orderRepository,
                          PancakeRepository pancakeRepository,
                          OrderLog orderLog) {
        this.orderRepository = orderRepository;
        this.pancakeRepository = pancakeRepository;
        this.orderLog = orderLog;
    }

    public OrderInfo createOrder(int building, int room) {
        Order order = new Order(building, room);
        orderRepository.save(order);
        return new OrderInfo(order.getId(), order.getBuilding(), order.getRoom());
    }

    public UUID startPancake(UUID orderId) {
        findModifiableOrder(orderId);
        return pancakeRepository.startPancake(orderId);
    }

    public void addIngredient(UUID orderId, UUID pancakeId, Ingredient ingredient) {
        validateUuid(orderId, "Order id");
        validateUuid(pancakeId, "Pancake id");

        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient must not be null");
        }

        findModifiableOrder(orderId);
        validatePendingPancakeOwnership(orderId, pancakeId);
        pancakeRepository.addIngredient(pancakeId, ingredient);
    }

    public void finishPancake(UUID orderId, UUID pancakeId) {
        validateUuid(orderId, "Order id");
        validateUuid(pancakeId, "Pancake id");

        Order order = findModifiableOrder(orderId);
        validatePendingPancakeOwnership(orderId, pancakeId);
        List<Ingredient> ingredients = pancakeRepository.finishPancake(pancakeId);

        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Cannot finish a pancake with no ingredients");
        }

        Pancake pancake = new Pancake(ingredients);
        pancakeRepository.addPancake(orderId, pancake);
        orderLog.logAddPancake(order, pancake.description(), pancakeRepository.findByOrderId(orderId));
    }

    public List<String> viewOrder(UUID orderId) {
        validateUuid(orderId, "Order id");

        return pancakeRepository.findByOrderId(orderId).stream()
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

        Order order = findModifiableOrder(orderId);
        pancakeRepository.removePancakes(orderId, description, count);
        orderLog.logRemovePancakes(order, description, count,
                pancakeRepository.findByOrderId(orderId));
    }

    public void completeOrder(UUID orderId) {
        validateUuid(orderId, "Order id");

        findOrder(orderId);
        orderRepository.markCompleted(orderId);
    }

    public Set<UUID> listCompletedOrders() {
        return orderRepository.getCompletedOrders();
    }

    public void prepareOrder(UUID orderId) {
        validateUuid(orderId, "Order id");
        orderRepository.markPrepared(orderId);
    }

    public Set<UUID> listPreparedOrders() {
        return orderRepository.getPreparedOrders();
    }

    public DeliveryInfo deliverOrder(UUID orderId) {
        validateUuid(orderId, "Order id");

        Order order = findOrder(orderId);
        orderRepository.markDelivered(orderId);
        List<String> pancakesToDeliver = viewOrder(orderId);
        orderLog.logDeliverOrder(order, pancakeRepository.findByOrderId(orderId));
        pancakeRepository.clearPendingByOrderId(orderId);
        pancakeRepository.removeAllByOrderId(orderId);
        orderRepository.delete(orderId);
        return new DeliveryInfo(order.getId(), order.getBuilding(), order.getRoom(), pancakesToDeliver);
    }

    public void cancelOrder(UUID orderId) {
        validateUuid(orderId, "Order id");

        Order order = findOrder(orderId);
        pancakeRepository.clearPendingByOrderId(orderId);
        pancakeRepository.removeAllByOrderId(orderId);
        orderRepository.delete(orderId);
        orderLog.logCancelOrder(order, List.of());
    }

    private Order findModifiableOrder(UUID orderId) {
        Order order = findOrder(orderId);
        OrderStatus status = orderRepository.getStatus(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (status != OrderStatus.OPEN) {
            throw new IllegalStateException("Cannot modify a completed or prepared order: " + orderId);
        }

        return order;
    }

    private void validatePendingPancakeOwnership(UUID orderId, UUID pancakeId) {
        if ( ! pancakeRepository.hasPending(pancakeId)) {
            throw new IllegalArgumentException("Pancake not found: " + pancakeId);
        }

        UUID ownerOrderId = pancakeRepository.getPendingOwner(pancakeId);
        if (!ownerOrderId.equals(orderId)) {
            throw new IllegalArgumentException("Pancake does not belong to order: " + pancakeId);
        }
    }

    private void validateUuid(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException(label + " must not be null");
        }
    }

    private Order findOrder(UUID orderId) {
        validateUuid(orderId, "Order id");

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
