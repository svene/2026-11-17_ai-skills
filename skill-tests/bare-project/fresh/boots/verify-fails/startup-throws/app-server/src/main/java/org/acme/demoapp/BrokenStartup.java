package org.acme.demoapp;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;

/** Planted: the app fails to boot, so the boot test must fail. */
class BrokenStartup {
    void onStart(@Observes StartupEvent event) {
        throw new IllegalStateException("PLANTED-BOOT-FAILURE");
    }
}
