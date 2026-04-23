package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceConcurrencyTest {

    private PancakeService pancakeService;

    @BeforeEach
    public void setUp() {
        pancakeService = new PancakeService();
    }

    @Test
    public void GivenMultipleThreads_WhenCreatingOrdersConcurrently_ThenAllOrdersCreated_Test()
            throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<OrderInfo> createdOrders = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int building = i + 1;
            int room     = i + 1;
            executor.submit(() -> {
                OrderInfo order = pancakeService.createOrder(building, room);
                synchronized (createdOrders) {
                    createdOrders.add(order);
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertEquals(threadCount, createdOrders.size());
    }

    @Test
    public void GivenMultipleThreads_WhenAddingPancakesToSameOrder_ThenAllPancakesAdded_Test()
            throws InterruptedException {
        int threadCount = 10;
        OrderInfo order = pancakeService.createOrder(1, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                UUID pancakeId = pancakeService.startPancake(order.id());
                pancakeService.addIngredient(order.id(), pancakeId, Ingredient.DARK_CHOCOLATE);
                pancakeService.finishPancake(order.id(), pancakeId);
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertEquals(threadCount, pancakeService.viewOrder(order.id()).size());
    }

    @Test
    public void GivenMultipleThreads_WhenCompletingOrdersConcurrently_ThenAllOrdersCompleted_Test()
            throws InterruptedException {
        int threadCount = 10;
        List<OrderInfo> orders = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            orders.add(pancakeService.createOrder(i + 1, i + 1));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (OrderInfo order : orders) {
            executor.submit(() -> {
                pancakeService.completeOrder(order.id());
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertEquals(threadCount, pancakeService.listCompletedOrders().size());
    }
}