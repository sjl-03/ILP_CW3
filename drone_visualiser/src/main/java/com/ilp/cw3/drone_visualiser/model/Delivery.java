package com.ilp.cw3.drone_visualiser.model;

import java.util.List;

public record Delivery(
        int deliveryId,
        List<Position> flightPath
) {
}
