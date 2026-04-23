package org.pancakelab.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.dto.DeliveryInfo;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PancakeServiceTest {
    private PancakeService pancakeService = new PancakeService();
    private OrderInfo      order          = null;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @Test
    @org.junit.jupiter.api.Order(10)
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        order = pancakeService.createOrder(10, 20);
        assertEquals(10, order.building());
        assertEquals(20, order.room());
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        addPancakes();
        List<String> ordersPancakes = pancakeService.viewOrder(order.id());
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);
    }

    @Test
    @org.junit.jupiter.api.Order(30)
    public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.id(), 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.id(), 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.id(), 1);
        List<String> ordersPancakes = pancakeService.viewOrder(order.id());
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);
    }

    @Test
    @org.junit.jupiter.api.Order(40)
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        pancakeService.completeOrder(order.id());
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrders.contains(order.id()));
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        pancakeService.prepareOrder(order.id());
        assertFalse(pancakeService.listCompletedOrders().contains(order.id()));
        assertTrue(pancakeService.listPreparedOrders().contains(order.id()));
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        List<String> pancakesToDeliver = pancakeService.viewOrder(order.id());
        DeliveryInfo deliveryInfo = pancakeService.deliverOrder(order.id());

        assertFalse(pancakeService.listCompletedOrders().contains(order.id()));
        assertFalse(pancakeService.listPreparedOrders().contains(order.id()));
        assertEquals(List.of(), pancakeService.viewOrder(order.id()));
        assertEquals(order.id(),        deliveryInfo.orderId());
        assertEquals(pancakesToDeliver, deliveryInfo.pancakes());
        order = null;
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        order = pancakeService.createOrder(10, 20);
        addPancakes();
        pancakeService.cancelOrder(order.id());

        assertFalse(pancakeService.listCompletedOrders().contains(order.id()));
        assertFalse(pancakeService.listPreparedOrders().contains(order.id()));
        assertEquals(List.of(), pancakeService.viewOrder(order.id()));
    }

    private void addPancakes() {
        for (int i = 0; i < 3; i++) {
            UUID p = pancakeService.startPancake(order.id());
            pancakeService.addIngredient(order.id(), p, Ingredient.DARK_CHOCOLATE);
            pancakeService.finishPancake(order.id(), p);
        }
        for (int i = 0; i < 3; i++) {
            UUID p = pancakeService.startPancake(order.id());
            pancakeService.addIngredient(order.id(), p, Ingredient.MILK_CHOCOLATE);
            pancakeService.finishPancake(order.id(), p);
        }
        for (int i = 0; i < 3; i++) {
            UUID p = pancakeService.startPancake(order.id());
            pancakeService.addIngredient(order.id(), p, Ingredient.MILK_CHOCOLATE);
            pancakeService.addIngredient(order.id(), p, Ingredient.HAZELNUTS);
            pancakeService.finishPancake(order.id(), p);
        }
    }
}