package uk.ac.ed.acp.cw2.services;

import uk.ac.ed.acp.cw2.data.Capabilities;
import uk.ac.ed.acp.cw2.data.Position;

public record MoveCostEstimator(GeometryService geometryService, double STEP) {
    public int estimateMove(Position a, Position b) {
        return (int) Math.ceil(geometryService.euclidianDistance(a, b) / STEP);
    }

    public double estimateCostLB(int moves, Capabilities cap, int nDeliveries) {
        double total = cap.costInitial() + cap.costFinal()
                + moves * cap.costPerMove();
        return total / nDeliveries;
    }
}
