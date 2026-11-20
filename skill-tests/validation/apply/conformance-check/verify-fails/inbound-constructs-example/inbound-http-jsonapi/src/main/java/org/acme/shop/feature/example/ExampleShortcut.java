package org.acme.shop.feature.example;

/** Planted: builds the domain value from untrusted input without parse(). */
public class ExampleShortcut {

    public static Example fromRequest(String name, int count) {
        return new Example(name, count);
    }
}
