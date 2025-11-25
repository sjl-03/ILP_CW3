package uk.ac.ed.acp.cw2.dto;

import uk.ac.ed.acp.cw2.data.Position;

public record DeliveryTarget(
        int id,
        Position delivery
) {
}
