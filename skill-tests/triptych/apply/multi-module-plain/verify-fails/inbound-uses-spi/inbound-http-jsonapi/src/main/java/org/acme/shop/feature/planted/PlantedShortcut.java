package org.acme.shop.feature.planted;

/** Planted violation: an inbound-module class reaching an outbound port (not a bean, so CDI stays satisfied). */
public class PlantedShortcut {
    public String load(PlantedStoreSPI store) {
        return store.load();
    }
}
