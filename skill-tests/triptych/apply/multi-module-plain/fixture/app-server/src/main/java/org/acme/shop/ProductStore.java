package org.acme.shop;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/** In-memory product storage. */
@ApplicationScoped
public class ProductStore {

    private final List<Product> products = new ArrayList<>();

    public synchronized List<Product> all() {
        return List.copyOf(products);
    }

    public synchronized void add(Product product) {
        products.removeIf(p -> p.sku().equals(product.sku()));
        products.add(product);
    }
}
