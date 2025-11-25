package uk.ac.ed.acp.cw2.dto;
import uk.ac.ed.acp.cw2.data.Position;

import java.util.List;

public record PositionsWTotalMove(
        int totalMove,
        List<Position> positions
) {
}
