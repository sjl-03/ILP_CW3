package uk.ac.ed.acp.cw2.data;
import jakarta.validation.constraints.NotNull;
public record Capabilities(
        @NotNull boolean cooling,
        @NotNull boolean heating,
        @NotNull double capacity,
        @NotNull int maxMoves,
        @NotNull double costPerMove,
        @NotNull double costInitial,
        @NotNull double costFinal
) {
}
