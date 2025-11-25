package uk.ac.ed.acp.cw2.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import javax.swing.*;
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
