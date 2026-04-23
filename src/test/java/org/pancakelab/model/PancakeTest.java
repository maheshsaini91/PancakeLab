package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.pancakelab.enums.Ingredient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeTest {

    @Test
    public void GivenValidIngredients_WhenCreatingPancake_ThenDescriptionIsCorrect_Test() {
        Pancake pancake = new Pancake(List.of(Ingredient.DARK_CHOCOLATE, Ingredient.WHIPPED_CREAM));
        assertEquals("Delicious pancake with dark chocolate, whipped cream!", pancake.description());
    }

    @Test
    public void GivenSingleIngredient_WhenCreatingPancake_ThenDescriptionIsCorrect_Test() {
        Pancake pancake = new Pancake(List.of(Ingredient.MILK_CHOCOLATE));
        assertEquals("Delicious pancake with milk chocolate!", pancake.description());
    }

    @Test
    public void GivenAllIngredients_WhenCreatingPancake_ThenIngredientsListIsCorrect_Test() {
        List<Ingredient> ingredients = List.of(Ingredient.MILK_CHOCOLATE, Ingredient.HAZELNUTS);
        Pancake pancake = new Pancake(ingredients);
        assertEquals(List.of("milk chocolate", "hazelnuts"), pancake.ingredients());
    }

    @Test
    public void GivenEmptyIngredients_WhenCreatingPancake_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Pancake(List.of()));
    }

    @Test
    public void GivenNullIngredients_WhenCreatingPancake_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Pancake(null));
    }
}
