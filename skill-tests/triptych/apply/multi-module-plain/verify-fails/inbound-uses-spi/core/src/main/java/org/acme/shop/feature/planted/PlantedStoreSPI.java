package org.acme.shop.feature.planted;

/** An outbound port, so the planted inbound class has something to bypass a Handler with. */
public interface PlantedStoreSPI {
    String load();
}
