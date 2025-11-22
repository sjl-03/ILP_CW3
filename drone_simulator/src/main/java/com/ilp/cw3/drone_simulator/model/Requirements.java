package com.ilp.cw3.drone_simulator.model;

import jakarta.validation.constraints.NotNull;

public record Requirements(
    @NotNull
    Double capacity,
    boolean cooling,
    boolean heating,
    Double maxCost
){
    public Requirements {
        if (cooling && heating){
            throw new IllegalArgumentException(
                    "Cannot have both cooling and heating requests"
            );
        }
    }
}
