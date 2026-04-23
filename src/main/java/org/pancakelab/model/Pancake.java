package org.pancakelab.model;

import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.List;
import java.util.UUID;

public class Pancake implements PancakeRecipe {

    private final List<Ingredient> ingredients;
    private UUID orderId;

    public Pancake(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty())
            throw new IllegalArgumentException("Pancake must have at least one ingredient");
        this.ingredients = List.copyOf(ingredients);
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    @Override
    public List<String> ingredients() {
        return ingredients.stream()
                .map(Ingredient::displayName)
                .toList();
    }
}
