package uk.ac.ed.acp.cw2.data;

import jakarta.validation.constraints.NotNull;

public record Position(@NotNull Double lng, @NotNull Double lat) {}