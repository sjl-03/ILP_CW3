package uk.ac.ed.acp.cw2.dto;

import uk.ac.ed.acp.cw2.data.Delivery;

import java.util.List;

public record DeliveryWTotalMove(
        int totalMove,
        List<Delivery> deliveries
) {
}
