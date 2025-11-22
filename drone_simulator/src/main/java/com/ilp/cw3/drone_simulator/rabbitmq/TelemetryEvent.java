package com.ilp.cw3.drone_simulator.rabbitmq;


import com.ilp.cw3.drone_simulator.model.DroneStatus;
import com.ilp.cw3.drone_simulator.model.Position;

import java.time.LocalDate;
import java.time.LocalTime;

public record TelemetryEvent(
        String droneId,
        Position position,
        int remainingMoves,
        DroneStatus status,
        Integer servicePointId,
        Integer dispatchId,
        LocalDate date,
        LocalTime time
) {
}
