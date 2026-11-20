package org.acme.shop.feature.order;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OrderMessageDeserializer extends ObjectMapperDeserializer<OrderMessage> {

    public OrderMessageDeserializer() {
        super(OrderMessage.class);
    }
}
