package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.pancakelab.dto.DeliveryInfo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryInfoTest {

    @Test
    public void GivenValidData_WhenCreatingDeliveryInfo_ThenFieldsAreCorrect_Test() {
        UUID id = UUID.randomUUID();
        List<String> pancakes = List.of("Delicious pancake with dark chocolate!");
        DeliveryInfo info = new DeliveryInfo(id, 2, 5, pancakes);
        assertEquals(id,       info.orderId());
        assertEquals(2,        info.building());
        assertEquals(5,        info.room());
        assertEquals(pancakes, info.pancakes());
    }

    @Test
    public void GivenSameData_WhenComparingDeliveryInfos_ThenTheyAreEqual_Test() {
        UUID id = UUID.randomUUID();
        List<String> pancakes = List.of("Delicious pancake with milk chocolate!");
        DeliveryInfo a = new DeliveryInfo(id, 1, 1, pancakes);
        DeliveryInfo b = new DeliveryInfo(id, 1, 1, pancakes);
        assertEquals(a, b);
    }
}
