package uk.ac.ed.acp.cw2.data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record Region(
        @NotNull String name,
        @NotNull List<@Valid Position> vertices
) {
    public Region {
        if (vertices == null || vertices.size() < 4) {
            throw new IllegalArgumentException("Invalid Region: need at least" +
                    " 3 vertices (closed)");
        }
        // must be closed (first == last) as per CW1
        if (!vertices.getFirst().equals(vertices.getLast())) {
            throw new IllegalArgumentException(
                    "Invalid Region: polygon not closed");
        }
    }
}
