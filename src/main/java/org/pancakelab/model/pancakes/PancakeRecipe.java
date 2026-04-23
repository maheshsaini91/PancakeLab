package org.pancakelab.model.pancakes;

import java.util.List;

public interface PancakeRecipe {
    default String description() {
        return "Delicious pancake with %s!".formatted(String.join(", ", ingredients()));
    }

    List<String> ingredients();
}