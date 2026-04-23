package org.pancakelab.model.pancakes;

import org.pancakelab.enums.Ingredient;

import java.util.List;

public class Pancake implements PancakeRecipe {

    private final List<Ingredient> ingredients;

    public Pancake(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty())
            throw new IllegalArgumentException("Pancake must have at least one ingredient");
        this.ingredients = List.copyOf(ingredients);
    }

    @Override
    public List<String> ingredients() {
        return ingredients.stream()
                .map(Ingredient::displayName)
                .toList();
    }
}