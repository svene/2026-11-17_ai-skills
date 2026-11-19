package org.acme.demoapp.feature.b;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BHandler {
    public String run() {
        return "b";
    }
}
