package org.acme.demoapp.feature.example;

/** Planted violation: an inbound-module class reaching an outbound port (not a bean, so CDI stays satisfied). */
public class ExampleShortcut {
    public String load(ExampleStoreSPI store) {
        return store.load();
    }
}
