package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceIngredientAPITest {

    private PancakeService pancakeService;
    private OrderInfo      order;

    @BeforeEach
    public void setUp() {
        pancakeService = new PancakeService();
        order          = pancakeService.createOrder(1, 1);
    }

    @Test
    public void GivenOrder_WhenStartingPancake_ThenPancakeIdReturned_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        assertNotNull(pancakeId);
    }

    @Test
    public void GivenStartedPancake_WhenAddingIngredients_ThenPancakeNotYetInOrder_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
        assertEquals(List.of(), pancakeService.viewOrder(order.id()));
    }

    @Test
    public void GivenStartedPancakeWithIngredients_WhenFinishing_ThenPancakeAppearsInOrder_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.WHIPPED_CREAM);
        pancakeService.finishPancake(order.id(), pancakeId);
        assertEquals(List.of("Delicious pancake with dark chocolate, whipped cream!"),
                pancakeService.viewOrder(order.id()));
    }

    @Test
    public void GivenMultiplePancakesBuilt_WhenFinished_ThenAllAppearInOrder_Test() {
        UUID p1 = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), p1, Ingredient.DARK_CHOCOLATE);
        pancakeService.finishPancake(order.id(), p1);

        UUID p2 = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), p2, Ingredient.MILK_CHOCOLATE);
        pancakeService.addIngredient(order.id(), p2, Ingredient.HAZELNUTS);
        pancakeService.finishPancake(order.id(), p2);

        assertEquals(List.of("Delicious pancake with dark chocolate!",
                        "Delicious pancake with milk chocolate, hazelnuts!"),
                pancakeService.viewOrder(order.id()));
    }

    @Test
    public void GivenStartedPancake_WhenFinishedWithNoIngredients_ThenExceptionThrown_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        assertThrows(IllegalStateException.class,
                () -> pancakeService.finishPancake(order.id(), pancakeId));
    }

    @Test
    public void GivenInvalidOrderId_WhenStartingPancake_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.startPancake(UUID.randomUUID()));
    }

    @Test
    public void GivenInvalidPancakeId_WhenAddingIngredient_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredient(order.id(), UUID.randomUUID(), Ingredient.DARK_CHOCOLATE));
    }

    @Test
    public void GivenPancakeStartedForDifferentOrder_WhenAddingIngredient_ThenExceptionThrown_Test() {
        OrderInfo otherOrder = pancakeService.createOrder(2, 2);
        UUID pancakeId = pancakeService.startPancake(order.id());
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredient(otherOrder.id(), pancakeId, Ingredient.DARK_CHOCOLATE));
    }

    @Test
    public void GivenPancakeStartedForDifferentOrder_WhenFinishingPancake_ThenExceptionThrown_Test() {
        OrderInfo otherOrder = pancakeService.createOrder(2, 2);
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.finishPancake(otherOrder.id(), pancakeId));
    }
}
