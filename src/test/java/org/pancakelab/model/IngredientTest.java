package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.pancakelab.enums.Ingredient;

import static org.junit.jupiter.api.Assertions.*;

public class IngredientTest {

    @Test
    public void GivenValidIngredientName_WhenLookingUp_ThenIngredientReturned_Test() {
        Ingredient ingredient = Ingredient.valueOf("DARK_CHOCOLATE");
        assertEquals(Ingredient.DARK_CHOCOLATE, ingredient);
    }

    @Test
    public void GivenAllIngredients_WhenCheckingNames_ThenCorrectDisplayNamesReturned_Test() {
        assertEquals("dark chocolate", Ingredient.DARK_CHOCOLATE.displayName());
        assertEquals("milk chocolate", Ingredient.MILK_CHOCOLATE.displayName());
        assertEquals("whipped cream",  Ingredient.WHIPPED_CREAM.displayName());
        assertEquals("hazelnuts",      Ingredient.HAZELNUTS.displayName());
    }

    @Test
    public void GivenInvalidIngredientName_WhenLookingUp_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> Ingredient.valueOf("MUSTARD"));
    }
}
