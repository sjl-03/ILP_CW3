package uk.ac.ed.acp.cw2.data;
import java.util.List;
public record DronesAvailability(
        String id,
        List<Availability> availability
) {
}
