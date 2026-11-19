package org.acme.shop.feature.a;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.shop.feature.b.BHandler;

/** Planted violation: one feature slice calling another. */
@ApplicationScoped
public class AHandler {
    public String run(BHandler other) {
        return other.run();
    }
}
