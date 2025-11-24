package com.ilp.cw3.drone_simulator.model;

import jakarta.validation.constraints.NotNull;

public record Drone(
        String name,
        @NotNull String id,
        Capabilities capability
) {}
