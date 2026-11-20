package org.acme.shop.feature.item;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Adds items to the stock. */
@ApplicationScoped
public class ItemHandler {

    private final List<Item> items = new CopyOnWriteArrayList<>();

    public String add(Item item) {
        items.add(item);
        return item.quantity() + " x " + item.name();
    }

    public List<Item> items() {
        return List.copyOf(items);
    }
}
