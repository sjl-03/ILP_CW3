package uk.ac.ed.acp.cw2.data;

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
