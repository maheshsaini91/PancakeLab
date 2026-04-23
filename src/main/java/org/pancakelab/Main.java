package org.pancakelab;

import org.pancakelab.dto.DeliveryInfo;
import org.pancakelab.dto.OrderInfo;
import org.pancakelab.enums.Ingredient;
import org.pancakelab.service.PancakeService;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        PancakeService service = new PancakeService();

        // Disciple creates an order
        OrderInfo order = service.createOrder(5, 12);
        System.out.println("Order created: building=" + order.building() + " room=" + order.room());

        // Disciple builds pancakes ingredient by ingredient
        UUID p1 = service.startPancake(order.id());
        service.addIngredient(order.id(), p1, Ingredient.DARK_CHOCOLATE);
        service.addIngredient(order.id(), p1, Ingredient.WHIPPED_CREAM);
        service.finishPancake(order.id(), p1);

        UUID p2 = service.startPancake(order.id());
        service.addIngredient(order.id(), p2, Ingredient.MILK_CHOCOLATE);
        service.addIngredient(order.id(), p2, Ingredient.HAZELNUTS);
        service.finishPancake(order.id(), p2);

        System.out.println("Pancakes ordered: " + service.viewOrder(order.id()));

        // Disciple completes the order
        service.completeOrder(order.id());
        System.out.println("Completed orders: " + service.listCompletedOrders());

        // Chef prepares it
        service.prepareOrder(order.id());
        System.out.println("Prepared orders: " + service.listPreparedOrders());

        // Delivery delivers it
        DeliveryInfo delivery = service.deliverOrder(order.id());
        System.out.println("Delivered to building=" + delivery.building()
                + " room=" + delivery.room()
                + " pancakes=" + delivery.pancakes());
    }
}