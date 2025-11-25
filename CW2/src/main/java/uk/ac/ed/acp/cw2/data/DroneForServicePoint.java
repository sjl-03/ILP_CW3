package uk.ac.ed.acp.cw2.data;

import java.util.List;

public record DroneForServicePoint (
        int servicePointId,
        List<DronesAvailability> drones

){}
