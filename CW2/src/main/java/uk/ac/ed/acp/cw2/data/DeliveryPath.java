package uk.ac.ed.acp.cw2.data;

import java.util.List;

public record DeliveryPath(
        double totalCost,
        int totalMoves,
        List<DronePath> dronePaths
) {
}
