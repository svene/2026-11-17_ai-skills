package org.acme.shop.feature.order;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Places orders. */
@ApplicationScoped
public class OrderHandler {

    private final List<Order> placed = new CopyOnWriteArrayList<>();

    public void place(Order order) {
        placed.add(order);
    }

    public List<Order> placed() {
        return List.copyOf(placed);
    }
}
