package uk.ac.ed.acp.cw2.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import uk.ac.ed.acp.cw2.data.Position;

public record NextPositionRequest(
        @Valid @NotNull Position start,
        @NotNull Double angle
) {
}
