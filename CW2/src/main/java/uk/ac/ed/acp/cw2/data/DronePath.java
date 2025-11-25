package uk.ac.ed.acp.cw2.data;

import java.util.List;

public record DronePath (
        String droneId,
        List<Delivery> deliveries
){}
