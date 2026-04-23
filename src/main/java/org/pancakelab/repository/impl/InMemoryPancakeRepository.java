package org.pancakelab.repository.impl;

import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.pancakes.PancakeRecipe;
import org.pancakelab.repository.PancakeRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryPancakeRepository implements PancakeRepository {
    private final Map<UUID, List<PancakeRecipe>> pancakes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingPancakes = new ConcurrentHashMap<>();
    private final Map<UUID, List<Ingredient>> pendingIngredients = new ConcurrentHashMap<>();

    @Override
    public void addPancake(UUID orderId, PancakeRecipe pancake) {
        pancakes.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(pancake);
    }

    @Override
    public List<PancakeRecipe> findByOrderId(UUID orderId) {
        return Collections.unmodifiableList(pancakes.getOrDefault(orderId, List.of()));
    }

    @Override
    public void removePancakes(UUID orderId, String description, int count) {
        List<PancakeRecipe> orderPancakes = pancakes.get(orderId);
        if (orderPancakes == null) {
            return;
        }

        AtomicInteger removed = new AtomicInteger(0);
        orderPancakes.removeIf(p -> p.description().equals(description) &&
                removed.getAndIncrement() < count);
    }

    @Override
    public void removeAllByOrderId(UUID orderId) {
        pancakes.remove(orderId);
    }

    @Override
    public UUID startPancake(UUID orderId) {
        UUID pancakeId = UUID.randomUUID();
        pendingPancakes.put(pancakeId, orderId);
        pendingIngredients.put(pancakeId, new CopyOnWriteArrayList<>());
        return pancakeId;
    }

    @Override
    public void addIngredient(UUID pancakeId, Ingredient ingredient) {
        List<Ingredient> ingredients = pendingIngredients.get(pancakeId);
        if (ingredients != null) {
            ingredients.add(ingredient);
        }
    }

    @Override
    public List<Ingredient> finishPancake(UUID pancakeId) {
        List<Ingredient> ingredients = pendingIngredients.remove(pancakeId);
        pendingPancakes.remove(pancakeId);
        return ingredients != null ? ingredients : List.of();
    }

    @Override
    public void clearPendingByOrderId(UUID orderId) {
        List<UUID> toRemove = pendingPancakes.entrySet().stream()
                .filter(e -> e.getValue().equals(orderId))
                .map(Map.Entry::getKey)
                .toList();
        toRemove.forEach(id -> {
            pendingPancakes.remove(id);
            pendingIngredients.remove(id);
        });
    }

    @Override
    public boolean hasPending(UUID pancakeId) {
        return pendingPancakes.containsKey(pancakeId);
    }

    @Override
    public UUID getPendingOwner(UUID pancakeId) {
        return pendingPancakes.get(pancakeId);
    }
}