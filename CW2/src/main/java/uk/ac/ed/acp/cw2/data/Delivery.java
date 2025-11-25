package uk.ac.ed.acp.cw2.data;

import java.util.List;

public record Delivery(
        int deliveryId,
        List<Position> flightPath
) {
}
