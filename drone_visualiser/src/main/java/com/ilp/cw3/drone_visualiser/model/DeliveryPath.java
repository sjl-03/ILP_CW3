package com.ilp.cw3.drone_visualiser.model;
import java.util.List;

public record DeliveryPath(
        double totalCost,
        int totalMoves,
        List<DronePath> dronePaths
) {
}
