package com.ilp.cw3.drone_visualiser.model;

import java.util.List;

public record DronePath (
        String droneId,
        List<Delivery> deliveries
){}
