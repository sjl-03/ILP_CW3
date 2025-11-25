package com.ilp.cw3.drone_visualiser.model;

public record DroneWebSocketMessage(
        String droneId,
        DroneStatus status,
        Position position,
        Integer dispatchId,
        int remainingMoves

) {
}
