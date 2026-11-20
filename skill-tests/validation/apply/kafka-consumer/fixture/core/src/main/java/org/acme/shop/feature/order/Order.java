package org.acme.shop.feature.order;

/** An order for some quantity of a product. */
public record Order(String product, int quantity) {
}
