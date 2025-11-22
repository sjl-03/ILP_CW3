package com.ilp.cw3.drone_simulator.model;
import jakarta.validation.constraints.NotNull;

public record Position(@NotNull Double lng, @NotNull Double lat) {}
