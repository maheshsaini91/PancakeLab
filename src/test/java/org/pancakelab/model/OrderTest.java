package org.pancakelab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    public void GivenValidBuildingAndRoom_WhenCreatingOrder_ThenOrderCreated_Test() {
        Order order = new Order(1, 1);
        assertEquals(1, order.getBuilding());
        assertEquals(1, order.getRoom());
    }

    @Test
    public void GivenZeroBuilding_WhenCreatingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Order(0, 1));
    }

    @Test
    public void GivenNegativeBuilding_WhenCreatingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Order(-1, 1));
    }

    @Test
    public void GivenZeroRoom_WhenCreatingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, 0));
    }

    @Test
    public void GivenNegativeRoom_WhenCreatingOrder_ThenExceptionThrown_Test() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, -1));
    }
}
