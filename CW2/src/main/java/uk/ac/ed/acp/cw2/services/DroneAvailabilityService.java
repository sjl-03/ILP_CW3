package uk.ac.ed.acp.cw2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.data.*;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class DroneAvailabilityService {
    private static final Logger logger =
            LoggerFactory.getLogger(DroneAvailabilityService.class);
    public Set<String> droneMeetRequirement(
            List<MedDispatchRec> medDispatchRecs, Set<Drone> drones
    ) {
        List<Requirements> requirements = medDispatchRecs.stream()
                .map(MedDispatchRec::requirements).toList();

        // note capacity cannot be null
        double totalRequiredCapacity = requirements.stream()
                .mapToDouble(Requirements::capacity)
                .sum();

        boolean requireCooling = requirements.stream()
                .map(Requirements::cooling).toList().contains(true);

        boolean requireHeating = requirements.stream()
                .map(Requirements::heating).toList().contains(true);

        if (requireCooling && requireHeating){
            throw new IllegalArgumentException(
                    "Cannot have both cooling and heating requests");
        }

        Set<String> result = drones.stream()
                .filter(drone -> drone.capability().capacity() >= totalRequiredCapacity)
                .filter(drone -> {
                    if (requireCooling) return drone.capability().cooling();
                    if (requireHeating) return drone.capability().heating();
                    return true;
                })
                .map(Drone::id)
                .collect(Collectors.toSet());

        logger.debug("Found {} capable drones for requirements (capacity {})",
                result.size(), totalRequiredCapacity);
        return result;
    }

    /**
     * Given set of capable drone, required dayOfWeek (optional) and time
     * (optional), check which of these are available for the given time
     * constraint.
     * return list of drone id
     */
    public Set<String> getAvailableDrones(
            Set<String> capableDrones,
            String dayOfWeek,
          LocalTime startTime, LocalTime endTime,
          Map<Integer, Set<String>> spToAvailableDroneId,
          List<DroneForServicePoint> droneForServicePoints)
    {
        // assume capable drone not null

        Set<String> availableDrones = new HashSet<>();
        boolean nullDayConstraint = (dayOfWeek == null);
        boolean nullTimeConstraint = (startTime == null || endTime == null);

        if (nullDayConstraint && nullTimeConstraint){
            availableDrones.addAll(capableDrones);
        }

        for (DroneForServicePoint servicePoint : droneForServicePoints) {
            int servicePointId = servicePoint.servicePointId();
            List<DronesAvailability> dronesAvailabilities =
                    servicePoint.drones();

            if (dronesAvailabilities == null){continue;}

            // for drones at each service point
            for (DronesAvailability droneAvailability : dronesAvailabilities) {
                final String droneId = droneAvailability.id();
                // drone not capable, go to next drone
                if (!capableDrones.contains(droneId)) continue;

                // Add capable droneId to servicePointToDroneId
                spToAvailableDroneId.computeIfAbsent(servicePointId,
                        val -> new HashSet<>()).add(droneId);

                // No timing to check if no tining constraints
                if (nullDayConstraint && nullTimeConstraint){
                    continue;
                }

                // Check time slots against present constraints
                boolean matches = false;
                for (Availability timeSlot : droneAvailability.availability()) {
                    if (timeSlot == null) continue;
                    String availableDayOfWeek = timeSlot.dayOfWeek();
                    LocalTime from = timeSlot.from();
                    LocalTime until = timeSlot.until();

                    boolean dayOK = nullDayConstraint
                            || (availableDayOfWeek.equalsIgnoreCase(dayOfWeek));

                    boolean timeOK = nullTimeConstraint
                            // slot.from <= startTime && slot.until >= endTime
                            || (!from.isAfter(startTime)
                            && !until.isBefore(endTime));

                    if (dayOK && timeOK) {
                        matches = true;
                        break; // this drone qualifies; no need to check more slots
                    }

                }
                if (matches){
                    availableDrones.add(droneId);
                }
            }
        }



        return availableDrones;
    }

    public Set<String> filterDronesByLBCost(
            Set<String> availableDrones, List<MedDispatchRec> recs,
            List<DroneServicePoint> servicePoints,
            Map<Integer, Set<String>> spToAvailableDroneId,
            Map<String, Drone> idToDrone,
            MoveCostEstimator moveCostEstimator
    ) {
        if (availableDrones == null || availableDrones.isEmpty())
            return Set.of();

        // start with all available drones and intersect per-record
        Set<String> result = new HashSet<>(availableDrones);
        int numOfRecs = recs.size();

        for (MedDispatchRec rec : recs) {
            Double maxCostReq = rec.requirements().maxCost();
            // skip if no cost constraint
            if (maxCostReq == null) continue;

            Set<String> okForThisRec = new HashSet<>();

            for (DroneServicePoint sp : servicePoints) {
                Set<String> dronesAtSp = spToAvailableDroneId.get(sp.id());
                if (dronesAtSp == null || dronesAtSp.isEmpty()) continue;

                int estMoves = moveCostEstimator.estimateMove(
                        sp.location(), rec.delivery());

                for (String droneId : dronesAtSp) {
                    // only consider drones that passed earlier checks
                    if (!availableDrones.contains(droneId)) continue;

                    Drone drone = idToDrone.get(droneId);
                    if (drone == null) continue;

                    double estCost = moveCostEstimator.estimateCostLB(estMoves,
                            drone.capability(),
                            numOfRecs);
                    if (estCost <= maxCostReq) {
                        okForThisRec.add(droneId);
                    }
                }
            }

            // require drone to satisfy this record too
            result.retainAll(okForThisRec);
            if (result.isEmpty()) break;
        }

        return result;
    }
}
