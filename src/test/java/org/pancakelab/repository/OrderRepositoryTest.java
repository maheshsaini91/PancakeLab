package org.pancakelab.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.model.Order;
import org.pancakelab.repository.impl.InMemoryOrderRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderRepositoryTest {

    private OrderRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    public void GivenOrder_WhenSaved_ThenCanBeFoundById_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        Optional<Order> found = repository.findById(order.getId());
        assertTrue(found.isPresent());
        assertEquals(order.getId(), found.get().getId());
        assertEquals(Optional.of(OrderStatus.OPEN), repository.getStatus(order.getId()));
    }

    @Test
    public void GivenNonExistentId_WhenFindById_ThenEmptyReturned_Test() {
        Optional<Order> found = repository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }

    @Test
    public void GivenSavedOrder_WhenDeleted_ThenNotFoundAnymore_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.delete(order.getId());
        assertFalse(repository.findById(order.getId()).isPresent());
    }

    @Test
    public void GivenOrder_WhenMarkedCompleted_ThenIsCompletedReturnsTrue_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        assertTrue(repository.isCompleted(order.getId()));
        assertFalse(repository.isPrepared(order.getId()));
        assertEquals(Optional.of(OrderStatus.COMPLETED), repository.getStatus(order.getId()));
    }

    @Test
    public void GivenCompletedOrder_WhenMarkedPrepared_ThenIsPreparedReturnsTrueAndNotCompleted_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        repository.markPrepared(order.getId());
        assertTrue(repository.isPrepared(order.getId()));
        assertFalse(repository.isCompleted(order.getId()));
        assertEquals(Optional.of(OrderStatus.PREPARED), repository.getStatus(order.getId()));
    }

    @Test
    public void GivenPreparedOrder_WhenMarkedDelivered_ThenRemovedFromPrepared_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        repository.markPrepared(order.getId());
        repository.markDelivered(order.getId());
        assertFalse(repository.isPrepared(order.getId()));
        assertEquals(Optional.of(OrderStatus.DELIVERED), repository.getStatus(order.getId()));
    }

    @Test
    public void GivenMultipleCompletedOrders_WhenListingCompleted_ThenAllReturned_Test() {
        Order o1 = new Order(1, 1);
        Order o2 = new Order(2, 2);
        repository.save(o1);
        repository.save(o2);
        repository.markCompleted(o1.getId());
        repository.markCompleted(o2.getId());
        Set<UUID> completed = repository.getCompletedOrders();
        assertTrue(completed.contains(o1.getId()));
        assertTrue(completed.contains(o2.getId()));
    }

    @Test
    public void GivenMultiplePreparedOrders_WhenListingPrepared_ThenAllReturned_Test() {
        Order o1 = new Order(1, 1);
        Order o2 = new Order(2, 2);
        repository.save(o1);
        repository.save(o2);
        repository.markCompleted(o1.getId());
        repository.markCompleted(o2.getId());
        repository.markPrepared(o1.getId());
        repository.markPrepared(o2.getId());
        Set<UUID> prepared = repository.getPreparedOrders();
        assertTrue(prepared.contains(o1.getId()));
        assertTrue(prepared.contains(o2.getId()));
    }

    @Test
    public void GivenCompletedOrder_WhenDeleted_ThenRemovedFromCompletedSet_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        repository.delete(order.getId());
        assertFalse(repository.getCompletedOrders().contains(order.getId()));
    }

    @Test
    public void GivenReturnedSet_WhenServiceStateChanges_ThenSnapshotDoesNotChange_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        Set<UUID> snapshot = repository.getCompletedOrders();

        Order second = new Order(2, 2);
        repository.save(second);
        repository.markCompleted(second.getId());

        assertTrue(snapshot.contains(order.getId()));
        assertFalse(snapshot.contains(second.getId()));
    }

    @Test
    public void GivenOpenOrder_WhenMarkedPrepared_ThenExceptionThrown_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        assertThrows(IllegalStateException.class, () -> repository.markPrepared(order.getId()));
    }

    @Test
    public void GivenPreparedOrder_WhenMarkedCompletedAgain_ThenExceptionThrown_Test() {
        Order order = new Order(1, 1);
        repository.save(order);
        repository.markCompleted(order.getId());
        repository.markPrepared(order.getId());
        assertThrows(IllegalStateException.class, () -> repository.markCompleted(order.getId()));
    }
}
