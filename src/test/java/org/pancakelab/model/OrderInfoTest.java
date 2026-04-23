package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.pancakelab.dto.OrderInfo;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderInfoTest {

    @Test
    public void GivenValidData_WhenCreatingOrderInfo_ThenFieldsAreCorrect_Test() {
        UUID id = UUID.randomUUID();
        OrderInfo info = new OrderInfo(id, 3, 7);
        assertEquals(id, info.id());
        assertEquals(3,  info.building());
        assertEquals(7,  info.room());
    }

    @Test
    public void GivenSameData_WhenComparingOrderInfos_ThenTheyAreEqual_Test() {
        UUID id = UUID.randomUUID();
        OrderInfo a = new OrderInfo(id, 1, 2);
        OrderInfo b = new OrderInfo(id, 1, 2);
        assertEquals(a, b);
    }
}
