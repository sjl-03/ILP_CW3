package com.ilp.cw3.drone_simulator.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record  DroneDetail (
        String id,
        int remainingMoves,
        int servicePointId,
        DroneStatus state,
        LocalDate leaveDate,
        LocalTime leaveTime

){
}
