package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceValidationTest {

    private PancakeService pancakeService;
    private OrderInfo      order;

    @BeforeEach
    public void setUp() {
        pancakeService = new PancakeService();
        order          = pancakeService.createOrder(1, 1);
    }

    @Test
    public void GivenNullIngredient_WhenAddingIngredient_ThenExceptionThrown_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredient(order.id(), pancakeId, null));
    }

    @Test
    public void GivenNonExistentOrderId_WhenCompletingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.completeOrder(UUID.randomUUID()));
    }

    @Test
    public void GivenOrderNotCompleted_WhenPreparingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalStateException.class,
                () -> pancakeService.prepareOrder(order.id()));
    }

    @Test
    public void GivenOrderCompleted_WhenRemovingPancakes_ThenExceptionThrown_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
        pancakeService.finishPancake(order.id(), pancakeId);
        pancakeService.completeOrder(order.id());
        assertThrows(IllegalStateException.class,
                () -> pancakeService.removePancakes(
                        "Delicious pancake with dark chocolate!", order.id(), 1));
    }

    @Test
    public void GivenOrderNotPrepared_WhenDeliveringOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalStateException.class,
                () -> pancakeService.deliverOrder(order.id()));
    }

    @Test
    public void GivenOrderCompleted_WhenAddingIngredient_ThenExceptionThrown_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.completeOrder(order.id());
        assertThrows(IllegalStateException.class,
                () -> pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE));
    }

    @Test
    public void GivenOrderCompleted_WhenFinishingPancake_ThenExceptionThrown_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
        pancakeService.completeOrder(order.id());
        assertThrows(IllegalStateException.class,
                () -> pancakeService.finishPancake(order.id(), pancakeId));
    }

    @Test
    public void GivenOrderPrepared_WhenStartingPancake_ThenExceptionThrown_Test() {
        pancakeService.completeOrder(order.id());
        pancakeService.prepareOrder(order.id());
        assertThrows(IllegalStateException.class,
                () -> pancakeService.startPancake(order.id()));
    }

    @Test
    public void GivenBlankDescription_WhenRemovingPancakes_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.removePancakes(" ", order.id(), 1));
    }

    @Test
    public void GivenInvalidCount_WhenRemovingPancakes_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.removePancakes("Delicious pancake with dark chocolate!", order.id(), 0));
    }

    @Test
    public void GivenCompletedOrdersSnapshot_WhenServiceStateChanges_ThenSnapshotDoesNotChange_Test() {
        pancakeService.completeOrder(order.id());
        Set<UUID> snapshot = pancakeService.listCompletedOrders();

        OrderInfo secondOrder = pancakeService.createOrder(2, 2);
        pancakeService.completeOrder(secondOrder.id());

        assertTrue(snapshot.contains(order.id()));
        assertFalse(snapshot.contains(secondOrder.id()));
    }

    @Test
    public void GivenPreparedOrdersSnapshot_WhenServiceStateChanges_ThenSnapshotDoesNotChange_Test() {
        pancakeService.completeOrder(order.id());
        pancakeService.prepareOrder(order.id());
        Set<UUID> snapshot = pancakeService.listPreparedOrders();

        OrderInfo secondOrder = pancakeService.createOrder(2, 2);
        pancakeService.completeOrder(secondOrder.id());
        pancakeService.prepareOrder(secondOrder.id());

        assertTrue(snapshot.contains(order.id()));
        assertFalse(snapshot.contains(secondOrder.id()));
    }

    @Test
    public void GivenCancelledOrderWithPendingPancake_WhenReusingPancakeId_ThenPancakeNotFound_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.cancelOrder(order.id());

        OrderInfo otherOrder = pancakeService.createOrder(3, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredient(otherOrder.id(), pancakeId, Ingredient.DARK_CHOCOLATE));
        assertTrue(exception.getMessage().contains("Pancake not found"));
    }

    @Test
    public void GivenDeliveredOrderWithPendingPancake_WhenReusingPancakeId_ThenPancakeNotFound_Test() {
        UUID pancakeId = pancakeService.startPancake(order.id());
        pancakeService.completeOrder(order.id());
        pancakeService.prepareOrder(order.id());
        pancakeService.deliverOrder(order.id());

        OrderInfo otherOrder = pancakeService.createOrder(3, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredient(otherOrder.id(), pancakeId, Ingredient.DARK_CHOCOLATE));
        assertTrue(exception.getMessage().contains("Pancake not found"));
    }
}
