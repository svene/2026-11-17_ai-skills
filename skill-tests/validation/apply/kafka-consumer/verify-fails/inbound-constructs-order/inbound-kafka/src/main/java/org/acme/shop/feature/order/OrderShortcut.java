package org.acme.shop.feature.order;

/** Planted: builds the domain value with its constructor instead of parse(). */
class OrderShortcut {

    Order shortcut() {
        return new Order("planted", 1);
    }
}
