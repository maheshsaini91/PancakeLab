package org.pancakelab.repository;

import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.List;
import java.util.UUID;

public interface PancakeRepository {
    void addPancake(UUID orderId, PancakeRecipe pancake);
    List<PancakeRecipe> findByOrderId(UUID orderId);
    void removePancakes(UUID orderId, String description, int count);
    void removeAllByOrderId(UUID orderId);

    UUID startPancake(UUID orderId);
    void addIngredient(UUID pancakeId, Ingredient ingredient);
    List<Ingredient> finishPancake(UUID pancakeId);
    void clearPendingByOrderId(UUID orderId);
    boolean hasPending(UUID pancakeId);
    UUID getPendingOwner(UUID pancakeId);
}