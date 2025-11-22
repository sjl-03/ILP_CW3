package com.ilp.cw3.drone_simulator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimulatedDelivery {
    private final int deliveryId;
    private final List<Position> flightPath;
    private int moveIndex = 0;

    public SimulatedDelivery(Delivery delivery) {
        this.deliveryId = delivery.deliveryId();
        this.flightPath = delivery.flightPath();
    }

    public Position nextPosition() {
        int index = moveIndex++;
        if (index >= flightPath.size()) {
            return null; // current delivery finished
        }
        return flightPath.get(index);
    }
}
