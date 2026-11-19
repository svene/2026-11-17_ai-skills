package org.acme.demoapp.feature.a;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.demoapp.feature.b.BHandler;

/** Planted violation: one feature slice calling another. */
@ApplicationScoped
public class AHandler {
    public String run(BHandler other) {
        return other.run();
    }
}
