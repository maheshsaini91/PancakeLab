package org.pancakelab.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.enums.Ingredient;
import org.pancakelab.model.pancakes.Pancake;
import org.pancakelab.repository.impl.InMemoryPancakeRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeRepositoryTest {

    private PancakeRepository repository;
    private UUID              orderId;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryPancakeRepository();
        orderId = UUID.randomUUID();
    }

    @Test
    public void GivenOrderId_WhenStartingPancake_ThenPancakeIdReturnedAndPending_Test() {
        UUID pancakeId = repository.startPancake(orderId);
        assertNotNull(pancakeId);
        assertTrue(repository.hasPending(pancakeId));
    }

    @Test
    public void GivenPendingPancake_WhenGettingOwner_ThenCorrectOrderIdReturned_Test() {
        UUID pancakeId = repository.startPancake(orderId);
        assertEquals(orderId, repository.getPendingOwner(pancakeId));
    }

    @Test
    public void GivenPendingPancake_WhenAddingIngredientAndFinishing_ThenIngredientsReturned_Test() {
        UUID pancakeId = repository.startPancake(orderId);
        repository.addIngredient(pancakeId, Ingredient.DARK_CHOCOLATE);
        repository.addIngredient(pancakeId, Ingredient.WHIPPED_CREAM);
        List<Ingredient> ingredients = repository.finishPancake(pancakeId);
        assertEquals(List.of(Ingredient.DARK_CHOCOLATE, Ingredient.WHIPPED_CREAM), ingredients);
    }

    @Test
    public void GivenFinishedPancake_WhenCheckingPending_ThenNotFound_Test() {
        UUID pancakeId = repository.startPancake(orderId);
        repository.addIngredient(pancakeId, Ingredient.DARK_CHOCOLATE);
        repository.finishPancake(pancakeId);
        assertFalse(repository.hasPending(pancakeId));
    }

    @Test
    public void GivenPancake_WhenAddedToOrder_ThenFoundByOrderId_Test() {
        repository.addPancake(orderId, new Pancake(List.of(Ingredient.DARK_CHOCOLATE)));
        assertEquals(1, repository.findByOrderId(orderId).size());
    }

    @Test
    public void GivenMultiplePancakes_WhenRemovingByDescription_ThenCorrectCountRemoved_Test() {
        for (int i = 0; i < 3; i++)
            repository.addPancake(orderId, new Pancake(List.of(Ingredient.DARK_CHOCOLATE)));
        repository.removePancakes(orderId, "Delicious pancake with dark chocolate!", 2);
        assertEquals(1, repository.findByOrderId(orderId).size());
    }

    @Test
    public void GivenOrderWithPancakes_WhenRemoveAll_ThenOrderEmpty_Test() {
        repository.addPancake(orderId, new Pancake(List.of(Ingredient.MILK_CHOCOLATE)));
        repository.removeAllByOrderId(orderId);
        assertEquals(List.of(), repository.findByOrderId(orderId));
    }

    @Test
    public void GivenPendingPancake_WhenClearPendingByOrderId_ThenPancakeRemoved_Test() {
        UUID pancakeId = repository.startPancake(orderId);
        repository.clearPendingByOrderId(orderId);
        assertFalse(repository.hasPending(pancakeId));
    }

    @Test
    public void GivenNonExistentOrderId_WhenFindByOrderId_ThenEmptyListReturned_Test() {
        assertEquals(List.of(), repository.findByOrderId(UUID.randomUUID()));
    }
}
