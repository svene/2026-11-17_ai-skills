package org.acme.shop.feature.order;

/** The wire format of topic "orders". */
public record OrderMessage(String product, int quantity) {
}
