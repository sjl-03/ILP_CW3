package com.ilp.cw3.drone_simulator.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record MedDispatchRec(
        @NotNull
        int id,
        LocalDate date,
        LocalTime time,
        @NotNull @Valid
        Requirements requirements,
        @NotNull
        Position delivery

) {
}
