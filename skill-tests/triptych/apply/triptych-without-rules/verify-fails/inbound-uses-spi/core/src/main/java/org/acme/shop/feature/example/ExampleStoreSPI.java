package org.acme.shop.feature.example;

/** An outbound port, so the planted inbound class has something to bypass the Handler with. */
public interface ExampleStoreSPI {
    String load();
}
