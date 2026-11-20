package org.acme.shop.feature.item;

/** Planted: builds the domain value with its constructor instead of parse(). */
class ItemShortcut {

    Item shortcut() {
        return new Item("planted", 1);
    }
}
