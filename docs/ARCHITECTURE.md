# PancakeLab — Architecture & Design Overview

## Overview
PancakeLab is a pure Java pancake ordering system for a Coding Dojo.
It supports the full order lifecycle: create → build → complete → prepare → deliver (or cancel).

---

## Package Structure

```
org.pancakelab/
├── dto/                       # API-facing data transfer objects
│   ├── OrderInfo.java         # returned on order creation
│   └── DeliveryInfo.java      # returned on delivery
├── enums/
│   └── Ingredient.java        # valid ingredient catalog
├── model/
│   ├── Order.java             # internal domain entity
│   └── pancakes/
│       ├── PancakeRecipe.java  # contract interface
│       └── Pancake.java       # single flexible pancake class
├── repository/
│   ├── OrderRepository.java   # order storage contract
│   ├── PancakeRepository.java # pancake storage contract
│   └── impl/
│       ├── InMemoryOrderRepository.java
│       └── InMemoryPancakeRepository.java
└── service/
    ├── PancakeService.java    # business logic & lifecycle
    └── OrderLog.java          # audit logging
```

---

## Order Lifecycle

```
CREATED ──► (add pancakes) ──► COMPLETED ──► PREPARED ──► DELIVERED
     └──────────────────────► CANCELLED (at any stage)
```

State transitions are strictly enforced — skipping steps throws `IllegalStateException`.

---

## Key Design Decisions

### 1. Ingredient enum over hardcoded strings
`Ingredient` enum is the single source of truth for valid ingredients.
Invalid combinations (e.g. mustard) are impossible to express — rejected at compile time.

### 2. Composition over inheritance
The original 5 hardcoded pancake classes (with an inheritance chain that introduced mustard)
are replaced by a single `Pancake` class built from a `List<Ingredient>`.

### 3. API never exposes domain objects
`PancakeService` returns only `OrderInfo` and `DeliveryInfo` records.
Internal `Order` and `Pancake` objects never leave the service layer.

### 4. Ingredient-by-ingredient API
Pancakes are built step by step through the API:
```java
UUID pancakeId = service.startPancake(orderId);
service.addIngredient(orderId, pancakeId, Ingredient.DARK_CHOCOLATE);
service.addIngredient(orderId, pancakeId, Ingredient.WHIPPED_CREAM);
service.finishPancake(orderId, pancakeId);
```
Pending pancakes are tracked internally and only committed to the order on `finishPancake`.

### 5. Repository pattern for separation of concerns
`PancakeService` contains only business logic.
All data storage is delegated to `OrderRepository` and `PancakeRepository` interfaces.
Current implementation is in-memory. Swapping to a database requires only a new `impl` class.

### 6. Thread safety
All in-memory collections use `ConcurrentHashMap`, `CopyOnWriteArrayList`,
and `ConcurrentHashMap.newKeySet()`. No external synchronization needed.

### 7. Immutable snapshots
`listCompletedOrders()` and `listPreparedOrders()` return `Set.copyOf()` —
callers receive a point-in-time snapshot that cannot mutate internal state.

---

## Assumptions

- Buildings and rooms are identified by positive integers only.
- A pancake must have at least one ingredient to be valid.
- An order can be cancelled at any lifecycle stage (pending, completed, or prepared).
- Cancelling an order also cleans up any pancakes currently being built (pending).
- A pancake started for one order cannot have ingredients added under a different order.
- Storage is in-memory only — data does not persist between application restarts.
- No authentication or role enforcement — any caller can invoke any API method.
- No maximum limit on pancakes per order or ingredients per pancake.

---

## Testing Strategy (TDD)

Every feature was written test-first (red → green → refactor).

| Test Class                        | What it covers                              |
|-----------------------------------|---------------------------------------------|
| `OrderTest`                       | Building/room validation                    |
| `IngredientTest`                  | Enum values and display names               |
| `PancakeTest`                     | Pancake creation, description, validation   |
| `OrderInfoTest`                   | DTO correctness and equality                |
| `DeliveryInfoTest`                | DTO correctness and equality                |
| `OrderRepositoryTest`             | In-memory order storage and state tracking  |
| `PancakeRepositoryTest`           | In-memory pancake storage and pending flow  |
| `PancakeServiceTest`              | Full order lifecycle (happy path)           |
| `PancakeServiceIngredientAPITest` | Ingredient-by-ingredient API                |
| `PancakeServiceValidationTest`    | All validation and state transition guards  |
| `PancakeServiceConcurrencyTest`   | Thread safety under concurrent access       |

---

## Requirements Coverage

| Requirement                        | How it is met                                                 |
|------------------------------------|---------------------------------------------------------------|
| Object-oriented programming        | Interface, enum, records, composition, repository pattern     |
| TDD                                | Every class has a failing test written before implementation  |
| Pure Java, no frameworks           | Only JUnit 5 (test scope) in pom.xml                         |
| API does not expose domain objects | `OrderInfo` and `DeliveryInfo` DTOs returned to callers       |
| No hardcoded recipes               | `Ingredient` enum + `Pancake(List<Ingredient>)`               |
| Ingredient-by-ingredient API       | `startPancake` → `addIngredient` → `finishPancake`            |
| Input validation                   | Constructor guards, null checks, state transition enforcement |
| Thread safety                      | Concurrent collections throughout all in-memory repositories  |
| UML documentation                  | `docs/class-diagram.puml`                                     |

---

## Constraints

- Pure Java 17 — no frameworks, no external production dependencies.
- JUnit 5 for testing only.
- Build tool: Maven.
