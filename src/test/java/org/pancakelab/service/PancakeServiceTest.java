package org.pancakelab.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.Order;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PancakeServiceTest {
    private PancakeService pancakeService = new PancakeService();
    private Order          order          = null;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @Test
    @org.junit.jupiter.api.Order(10)
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        order = pancakeService.createOrder(10, 20);
        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        addPancakes();
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());
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
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.getId(), 1);
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);
    }

    @Test
    @org.junit.jupiter.api.Order(40)
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        pancakeService.completeOrder(order.getId());
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrders.contains(order.getId()));
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        pancakeService.prepareOrder(order.getId());
        assertFalse(pancakeService.listCompletedOrders().contains(order.getId()));
        assertTrue(pancakeService.listPreparedOrders().contains(order.getId()));
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        List<String> pancakesToDeliver = pancakeService.viewOrder(order.getId());
        Object[] deliveredOrder = pancakeService.deliverOrder(order.getId());

        assertFalse(pancakeService.listCompletedOrders().contains(order.getId()));
        assertFalse(pancakeService.listPreparedOrders().contains(order.getId()));
        assertEquals(List.of(), pancakeService.viewOrder(order.getId()));
        assertEquals(order.getId(), ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, (List<String>) deliveredOrder[1]);
        order = null;
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        order = pancakeService.createOrder(10, 20);
        addPancakes();
        pancakeService.cancelOrder(order.getId());

        assertFalse(pancakeService.listCompletedOrders().contains(order.getId()));
        assertFalse(pancakeService.listPreparedOrders().contains(order.getId()));
        assertEquals(List.of(), pancakeService.viewOrder(order.getId()));
    }

    private void addPancakes() {
        pancakeService.addPancake(order.getId(), List.of(Ingredient.DARK_CHOCOLATE), 3);
        pancakeService.addPancake(order.getId(), List.of(Ingredient.MILK_CHOCOLATE), 3);
        pancakeService.addPancake(order.getId(), List.of(Ingredient.MILK_CHOCOLATE, Ingredient.HAZELNUTS), 3);
    }
}