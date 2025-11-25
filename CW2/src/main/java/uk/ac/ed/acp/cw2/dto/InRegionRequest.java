package uk.ac.ed.acp.cw2.dto;

import jakarta.validation.Valid;
import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.Region;
import jakarta.validation.constraints.NotNull;

public record InRegionRequest(
        @Valid @NotNull Position position,
        @Valid @NotNull Region region
) {}