package com.ilp.cw3.drone_simulator.model;

import java.util.List;

public record Delivery(
        int deliveryId,
        List<Position> flightPath
) {
}
