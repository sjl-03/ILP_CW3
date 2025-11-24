package com.ilp.cw3.drone_simulator.model;
import jakarta.validation.constraints.NotNull;

public record Capabilities(
        @NotNull boolean cooling,
        @NotNull boolean heating,
        @NotNull double capacity,
        @NotNull int maxMoves,
        @NotNull double costPerMove,
        @NotNull double costInitial,
        @NotNull double costFinal
) {
}
