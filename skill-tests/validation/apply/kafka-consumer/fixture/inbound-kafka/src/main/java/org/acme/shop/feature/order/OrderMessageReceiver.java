package org.acme.shop.feature.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/** Consumes topic "orders" (channel "orders-in"). */
@ApplicationScoped
public class OrderMessageReceiver {

    @Inject
    OrderHandler orderHandler;

    @Incoming("orders-in")
    public void receive(OrderMessage message) {
        orderHandler.place(new Order(message.product(), message.quantity()));
    }
}
