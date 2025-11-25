package uk.ac.ed.acp.cw2.services;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.*;
import uk.ac.ed.acp.cw2.data.geoJson.*;
import uk.ac.ed.acp.cw2.dto.DeliveryTarget;
import uk.ac.ed.acp.cw2.dto.DeliveryWTotalMove;
import uk.ac.ed.acp.cw2.dto.PositionsWTotalMove;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PathPlanningServiceImpl implements PathPlanningService {
    private static final double STEP = 0.00015;
    private static final double ANGLE_CONSTRAINT = 22.5;
    private final DroneQueriesService droneQueriesService;
    private final MoveCostEstimator moveCostEstimator;
    private final IlpRestClient ilpRestClient;
    private final GeometryService geometryService;
    private final DroneAvailabilityService droneAvailabilityService;

    public PathPlanningServiceImpl(IlpRestClient ilpRestClient,
                                   GeometryService geometryService,
                                   DroneQueriesService droneQueriesService) {
        this.ilpRestClient = ilpRestClient;
        this.geometryService = geometryService;
        this.droneQueriesService = droneQueriesService;
        this.moveCostEstimator = new MoveCostEstimator(geometryService, STEP);
        this.droneAvailabilityService = new DroneAvailabilityService();
    }

    // Check all date are the same, and return correct date or null if no
    // constraints on date
    private LocalDate validateDate (List<MedDispatchRec> medDispatchRecs) {
        List<LocalDate> dates = medDispatchRecs.stream()
                .map(MedDispatchRec::date)
                .filter(Objects::nonNull)
                .toList();
        if (dates.isEmpty()) {return null;}

        LocalDate earliestDate = Collections.min(dates);
        LocalDate latestDate = Collections.max(dates);

        if (earliestDate.isEqual(latestDate)) {
            return earliestDate;
        }
        else  {
            throw new IllegalArgumentException("Date not consistent");
        }
    }


    @Override
    public List<String> queryAvailableDrones (List<MedDispatchRec> medDispatchRecs){
        if (medDispatchRecs.isEmpty()) {
            throw new  IllegalArgumentException("No med dispatch recs found");
        }

        // build new list and maps
        var ilpSnapshot = ilpRestClient.getIlpServiceSnapshot();
        List<DroneServicePoint> servicePoints = ilpSnapshot.servicePoints();
        List<Drone> drones = ilpSnapshot.drones();
        Map<String, Drone> idToDrone = drones.stream().collect(Collectors.toMap(
                Drone::id,
                d -> d
        ));
        Map<Integer, Set<String>> spToAvailableDroneId = new HashMap<>();

        return new ArrayList<>(queryAvailableDronesInternal(
                medDispatchRecs, spToAvailableDroneId, servicePoints, idToDrone,
                ilpSnapshot.dronesForServicePoints()
        ));
    }

    private Set<String> queryAvailableDronesInternal(
            List<MedDispatchRec> medDispatchRecs,
            Map<Integer, Set<String>> spToAvailableDroneId,
            List<DroneServicePoint> servicePoints,
            Map<String, Drone> idToDrone,
            List<DroneForServicePoint> dronesForServicePoints
    ){
        // Determine capable drones--------------
        Set<String> capableDrones =
                droneAvailabilityService.droneMeetRequirement(
                        medDispatchRecs, new HashSet<>(idToDrone.values()));
        if (capableDrones.isEmpty()) return Set.of();

        // Group dispatches by date
        Map<Optional<LocalDate>, List<MedDispatchRec>> groupedByDate =
                medDispatchRecs.stream()
                        .collect(Collectors.groupingBy(
                                rec ->
                                        Optional.ofNullable(rec.date())
                        ));
        // initialise spToAvailableDrone to null
        spToAvailableDroneId.clear();
        boolean firstIteration = true;

        for (Map.Entry<Optional<LocalDate>, List<MedDispatchRec>> entry
                : groupedByDate.entrySet()){

            LocalDate date = entry.getKey().orElse(null);
            List<MedDispatchRec> recsForDate = entry.getValue();

            String dayOfWeek =  (date == null) ?
                    null : date.getDayOfWeek().toString();

            // Find date and time bounds--------------
            List<LocalTime> times = recsForDate.stream()
                    .map(MedDispatchRec::time)
                    .filter(Objects::nonNull)
                    .sorted().toList();
            LocalTime startTime = times.isEmpty() ? null : times.get(0);
            LocalTime endTime = times.isEmpty() ? null : times.get(times.size() - 1);


            // Now check which of the capableDrones are available for given time
            Map<Integer, Set<String>> tempSpToAvailable = new HashMap<>();
            // populates tempSpToAvailable
            Set<String> availableDronesForDate =
                    droneAvailabilityService.getAvailableDrones(
                            capableDrones, dayOfWeek, startTime, endTime,
                            tempSpToAvailable, dronesForServicePoints);

            capableDrones.retainAll(availableDronesForDate);
            if (capableDrones.isEmpty()) {
                spToAvailableDroneId.clear();
                return Set.of();
            }

            if (firstIteration) {
                for (Map.Entry<Integer, Set<String>> tempEntry
                        : tempSpToAvailable.entrySet()) {

                    spToAvailableDroneId.put(tempEntry.getKey(),
                            new HashSet<>(tempEntry.getValue()));
                }

                firstIteration = false;
            } else {
                for (Integer spId : spToAvailableDroneId.keySet()) {
                    Set<String> curr = tempSpToAvailable.get(spId);
                    Set<String> prev = spToAvailableDroneId.get(spId);

                    if (curr == null) spToAvailableDroneId.remove(spId);
                    else {
                        prev.retainAll(curr);

                        if(prev.isEmpty()) {
                            spToAvailableDroneId.remove(spId);
                        } else {
                            spToAvailableDroneId.put(spId, curr);
                        }
                    }
                }
            }
        }



        return droneAvailabilityService.filterDronesByLBCost(
                capableDrones, medDispatchRecs, servicePoints,
                spToAvailableDroneId, idToDrone, moveCostEstimator);
    }

    private double round(double value){
        return Math.round(value * 1000_000.0) / 1000_000.0;
    }

    public List<Position> getNeighbours(Position currentPos,
                                        List<Region> restrictedRegions){
        List<Position> neighbours = new ArrayList<>();
        for (double angle = 0; angle < 360; angle += ANGLE_CONSTRAINT){
            Position nextPos = geometryService.nextPosition(currentPos, angle);
            nextPos = new Position(round(nextPos.lng()), round(nextPos.lat()));

            boolean inRestrictedRegion = false;
            for(Region restrictedRegion: restrictedRegions){
                if (geometryService.isInRegion(nextPos, restrictedRegion)
                        || pathThroughRegion(currentPos, nextPos, restrictedRegion)){
                    inRestrictedRegion = true;
                    break;
                }
            }
            if (!inRestrictedRegion){
                neighbours.add(nextPos);
            }
        }

        return neighbours;
    }

    private boolean pathThroughRegion(
            Position currentPos, Position nextPos, Region restrictedRegion){

        List<Position> vertices = restrictedRegion.vertices();
        for (int i = 0; i<vertices.size()-1; i++){
            if (segmentsIntersect(currentPos, nextPos, vertices.get(i),
                    vertices.get(i+1))){
                return true;
            }
        }

        return false;
    }

    private boolean segmentsIntersect(
            Position segment1a, Position segment1b,
            Position segment2a, Position segment2b){

        // https://stackoverflow.com/a/3842240

        double area1a = area2signed(segment1a, segment1b, segment2a);
        double area1b = area2signed(segment1a, segment1b, segment2b);
        double area2a = area2signed(segment2a, segment2b, segment1a);
        double area2b = area2signed(segment2a, segment2b, segment1b);

        // proper intersection
        // Check if same sign
        if (((area1a < 0) != (area1b < 0)) &&
                ((area2a < 0) != (area2b < 0))) {
            return true;
        }

        // 2a lies on 1a and 1b
        if (area1a == 0 && geometryService.pointOnEdge(
                segment2a, segment1a, segment1b)) return true;
        if (area1b == 0 && geometryService.pointOnEdge(
                segment2b, segment1a, segment1b)) return true;
        if (area2a == 0 && geometryService.pointOnEdge(
                segment1a, segment2a, segment2b)) return true;
        if (area2b == 0 && geometryService.pointOnEdge(
                segment1b, segment2a, segment2b)) return true;


        return false;
    }


    // 2 times (signed) area of triangle by using the fact cross-product is
    // area of parallelogram
    private double area2signed (Position a, Position b, Position c){
        double x_ba = b.lng() - a.lng();
        double y_ba = b.lat() - a.lat();
        double x_ca = c.lng() - a.lng();
        double y_ca = c.lat() - a.lat();
        // cross product (x_ba,y_ba)x(x_ca,y_ca)
        return (x_ba * y_ca) - (y_ba * x_ca);
    }

    private double heuristic (Position a, Position b){
        // heuristic in "number of steps" so it matches g (one step per neighbour)
        return geometryService.euclidianDistance(a, b) / STEP;
    }

    private PositionsWTotalMove calculatePath (Map<Position, Position> parent,
                                               Position start, Position goal){
        LinkedList<Position> path = new LinkedList<>();
        int totalMove = 0;
        Position current = goal;
        path.add(current);

        while (current != null && !current.equals(start)){
            current = parent.get(current);
            if (current != null){
                path.addFirst(current);
                totalMove += 1;
            }
        }
        if (path.isEmpty() || !path.getFirst().equals(start)){
            throw new IllegalArgumentException("No path found");
        }
        return new PositionsWTotalMove(totalMove, path);
    }

    /**
     * A* algorithm. Given two points, find the best route from a to b,
     * without going into restricted areas
     */
    public PositionsWTotalMove planPath (Position start, Position goal,
                                         List<Region> restrictedRegions){

        Map<Position, Double> cost = new HashMap<>();
        Map<Position, Double> fscore = new HashMap<>();
        Map<Position, Position> parent = new HashMap<>();

        PriorityQueue<Position> q =
                new PriorityQueue<>(Comparator.comparingDouble(
                        p -> fscore.getOrDefault(
                                p, Double.POSITIVE_INFINITY)));

        Set<Position> explored = new HashSet<>();

        cost.put(start, 0.0); // gscore
        fscore.put(start, heuristic(start, goal));
        parent.put(start, null);
        q.add(start);

        // for each node v in Graph.Nodes do
        while(!q.isEmpty()){
            Position curr = q.poll(); // extract min

            if (geometryService.isCloseTo(curr, goal))
                return calculatePath(parent, start, curr);

            // skip if explored
            if (explored.contains(curr)) continue;

            explored.add(curr);

            for (Position neighbour : getNeighbours(curr, restrictedRegions)){
                // skip if explored
                if (explored.contains(neighbour)) continue;

                // each neighbour is one constant-size step
                double newCost = cost.getOrDefault(
                        curr, Double.POSITIVE_INFINITY);

                if (newCost < cost.getOrDefault(
                        neighbour, Double.POSITIVE_INFINITY)){
                    parent.put(neighbour, curr);
                    cost.put(neighbour, newCost);
                    fscore.put(neighbour,
                            newCost + heuristic(neighbour, goal));
                    q.add(neighbour);
                }

            }
        }
        throw new IllegalStateException(
                "No path found between" + start + " and " + goal);
    }


    /**
     * Assume list of record are delivered with one drone
     * @param medDispatchRecs dispatch records
     * @return DeliveryPath
     */
    @Override
    public DeliveryPath calcDeliveryPath (List<MedDispatchRec> medDispatchRecs)
    {
        var ilpSnapshot = ilpRestClient.getIlpServiceSnapshot();
        List<DroneServicePoint> servicePoints = ilpSnapshot.servicePoints();
        List<Drone> allDrones = ilpSnapshot.drones();
        Map<String, Drone> idToDrone = allDrones.stream().collect(Collectors.toMap(
                Drone::id,
                d -> d
        ));
        List<Region> restrictedRegion = ilpSnapshot.restrictedRegions();
        List<DroneForServicePoint> dronesForServicePoints =
                ilpSnapshot.dronesForServicePoints();


        List<List<MedDispatchRec>> medDispatchByDate =
                splitByDate(medDispatchRecs);
        return combineDeliveryPath(medDispatchByDate, restrictedRegion,
                servicePoints, idToDrone, allDrones,
                dronesForServicePoints);
    }

    private DeliveryPath calcDeliveryPathImpl(
            List<MedDispatchRec> medDispatchRecs,
            List<Region> restrictedRegion,
            List<DroneServicePoint> servicePoints,
            Map<String, Drone> idToDrone,
            List<Drone> allDrones,
            List<DroneForServicePoint> dronesForServicePoints
    ) {

        Map<Integer, Set<String>> spToAvailableDroneId = new HashMap<>();

        // assume only one drone
        Set<String> availableDrones =
                queryAvailableDronesInternal(
                        medDispatchRecs, spToAvailableDroneId, servicePoints,
                        idToDrone, dronesForServicePoints);

        // Now spToAvailableDroneId is populated and ready to use

        if (availableDrones.isEmpty()){
            // split by heating and cooling
            List<List<MedDispatchRec>> tempGroups =
                    splitByTemperature(medDispatchRecs);
            if (tempGroups.size() == 1) {
                // nothing to split by temp → size split
                tempGroups = splitByAlternativeServicePoint(
                        medDispatchRecs,
                        findBestSPToSplitRec(
                                medDispatchRecs,
                                servicePoints),
                        servicePoints);
            }
            return combineDeliveryPath(tempGroups, restrictedRegion,
                    servicePoints, idToDrone, allDrones,
                    dronesForServicePoints);
        }
        else {
            // available drone not empty
            return calcDronePathSingleDrone(
                    medDispatchRecs, restrictedRegion, servicePoints,
                    spToAvailableDroneId, idToDrone, allDrones,
                    dronesForServicePoints, availableDrones);
        }
    }

    private DeliveryPath combineDeliveryPath (
        List<List<MedDispatchRec>> groups,
        List<Region> restrictedRegion,
        List<DroneServicePoint> servicePoints,
        Map<String, Drone> idToDrone,
        List<Drone> allDrones,
        List<DroneForServicePoint> dronesForServicePoints
    ){
        List<DronePath> dronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMove = 0;
        for (List<MedDispatchRec> group : groups) {
            DeliveryPath partial = calcDeliveryPathImpl(
                    group, restrictedRegion,
                    servicePoints, idToDrone, allDrones,
                    dronesForServicePoints);
            dronePaths.addAll(partial.dronePaths());
            totalMove += partial.totalMoves();
            totalCost += partial.totalCost();
        }
        return new DeliveryPath(totalCost, totalMove, dronePaths);
    }

    public DeliveryPath calcDronePathSingleDrone(
            List<MedDispatchRec> medDispatchRecs,
            List<Region> restrictedRegion,
            List<DroneServicePoint> servicePoints,
            Map<Integer, Set<String>> spToAvailableDroneId,
            Map<String, Drone> idToDrone,
            List<Drone> allDrones,
            List<DroneForServicePoint> dronesForServicePoints,
            Set<String> availableDrones
    )
    {
        // smallest maxCost from medDispatchRecs
        double maxCostReq = findMinimumMaxCostReq(medDispatchRecs);

        // choose best service point
        DroneServicePoint bestServicePoint = findBestSP(
                medDispatchRecs, servicePoints, spToAvailableDroneId);

        List<DeliveryTarget> deliveryTargets = medDispatchRecs.stream()
                .map(record ->
                        new DeliveryTarget(
                                record.id(),
                                record.delivery()
                        )).toList();

        List<DeliveryTarget> deliveryRoute =
                TSPGreedyNN(bestServicePoint.location(), deliveryTargets);

        int estTotalMove = estimateTotalMove(deliveryRoute);
        Optional<Drone> canFly = estimateIfDroneCanFly(
                spToAvailableDroneId.get(bestServicePoint.id()),
                estTotalMove);

        // none of the candidate drones can make this flight
        if (canFly.isEmpty()) {
            List<List<MedDispatchRec>> groups =
                    splitByAlternativeServicePoint(
                            medDispatchRecs,
                            bestServicePoint.location(), servicePoints);
            return combineDeliveryPath(groups, restrictedRegion,
                    servicePoints, idToDrone, allDrones,
                    dronesForServicePoints);
        }

        // path plan
        DeliveryWTotalMove deliveriesWMove = planFlightPathSingleDrone(
                deliveryRoute, restrictedRegion);
        List<Delivery> deliveries = deliveriesWMove.deliveries();
        int droneTotalMove = deliveriesWMove.totalMove();

        Set<String> possibleDrones =
                spToAvailableDroneId.get(bestServicePoint.id());
        // find suitable drone
        for (String droneId : possibleDrones){
            // Drone can fly multiple times
            Drone drone = idToDrone.get(droneId);
            if (drone == null){continue;}
            if (!availableDrones.contains(droneId)) {continue;}

            Capabilities droneCapabilities = drone.capability();
            if (droneCapabilities.maxMoves() < droneTotalMove){
                // maxMove not satisfied
                continue;
            }

            double droneTotalCost =
                    (droneCapabilities.costPerMove() * droneTotalMove)
                            + droneCapabilities.costInitial()
                            + droneCapabilities.costFinal();

            // maxCost requirement met
            if ((droneTotalCost / medDispatchRecs.size()) <= maxCostReq){
                // Same drone can fly multiple times, no need to set droneUsed
                return new DeliveryPath(
                        droneTotalCost, droneTotalMove,
                        List.of(new DronePath(droneId, deliveries))
                );
            }
            // continue if maxCost requirement not met
        }
        // maxCost or  requirement not met
        // Split
        List<List<MedDispatchRec>> splitMedDispatchRec =
                splitByAlternativeServicePoint(
                        medDispatchRecs, bestServicePoint.location(),
                        servicePoints);

        return combineDeliveryPath(splitMedDispatchRec, restrictedRegion,
                servicePoints, idToDrone, allDrones,
                dronesForServicePoints);
    }

    private DroneServicePoint findBestSP(
            List<MedDispatchRec> medDispatchRecs,
            List<DroneServicePoint> servicePoints,
            Map<Integer, Set<String>> spToAvailableDroneId
    ) {
        Position bestServicePointPos = null;
        String bestServicePointName = null;
        int bestServicePointId = -1;
        double shortestDistance = Double.MAX_VALUE;

        for (DroneServicePoint servicePoint: servicePoints){

            // skip service points without capable drones
            if (!spToAvailableDroneId.containsKey(servicePoint.id())){
                continue;
            }

            double distanceSum = 0;
            for (MedDispatchRec medDispatchRec: medDispatchRecs){
                distanceSum += geometryService.euclidianDistance(
                        medDispatchRec.delivery(), servicePoint.location()
                );
            }
            if (distanceSum < shortestDistance){
                shortestDistance = distanceSum;
                bestServicePointPos = servicePoint.location();
                bestServicePointName = servicePoint.name();
                bestServicePointId = servicePoint.id();
            }
        }

        if (bestServicePointId == -1 || bestServicePointPos == null) {
            throw new IllegalStateException(
                    "No service point available for the given dispatches");
        }

        return new DroneServicePoint(
                bestServicePointName, bestServicePointId, bestServicePointPos
        );
    }

    private Position findBestSPToSplitRec(
            List<MedDispatchRec> medDispatchRecs,
            List<DroneServicePoint> servicePoints
    ) {
        Position bestServicePointPos = null;
        double shortestDistance = Double.MAX_VALUE;

        for (DroneServicePoint servicePoint: servicePoints){

            double distanceSum = 0;
            for (MedDispatchRec medDispatchRec: medDispatchRecs){
                distanceSum += geometryService.euclidianDistance(
                        medDispatchRec.delivery(), servicePoint.location()
                );
            }
            if (distanceSum < shortestDistance){
                shortestDistance = distanceSum;
                bestServicePointPos = servicePoint.location();
            }
        }

        if (bestServicePointPos == null) {
            throw new IllegalStateException(
                    "Error no service point to split");
        }

        return bestServicePointPos;
    }

    private double findMinimumMaxCostReq(List<MedDispatchRec> medDispatchRecs) {
        double maxCostReq = Double.POSITIVE_INFINITY;

        for (MedDispatchRec medDispatchRec: medDispatchRecs){
            Double currMaxCost = medDispatchRec.requirements().maxCost();

            if (currMaxCost != null
                    && currMaxCost < maxCostReq){
                maxCostReq = currMaxCost;
            }
        }
        if (maxCostReq <= 0)
            throw new IllegalArgumentException("max cost cannot be <= 0");

        return maxCostReq;
    }

    private Optional<Drone> estimateIfDroneCanFly(Set<String> ids, int estMoves) {
        if (ids == null) return Optional.empty(); // no drones at this service point

        for (String id : ids) {
//            if (droneUsed.contains(id)) continue; // skip already assigned drones

            Drone d = droneQueriesService.getDroneDetails(id);
            int maxMoves = d.capability().maxMoves();

            // Found a drone that can handle this route
            if (maxMoves >= estMoves) return Optional.of(d);
        }

        // None found
        return Optional.empty();
    }


    private List<List<MedDispatchRec>> splitIfTooLarge(
            List<MedDispatchRec> medDispatchRecs) {

        int n = medDispatchRecs.size();
        int groupSize = (int) Math.ceil((double)n / 2);
        List<List<MedDispatchRec>> result = new ArrayList<>();

        for (int i = 0; i < n; i += groupSize) {
            result.add(medDispatchRecs.subList(i, Math.min(i + groupSize, n)));
        }
        return result;
    }

    List<List<MedDispatchRec>> splitByDate(List<MedDispatchRec> medDispatchRecs) {

        // Group by date
       Map<Optional<LocalDate>, List<MedDispatchRec>> grouped =
                medDispatchRecs.stream()
                        .collect(Collectors.groupingBy(
                                rec -> Optional.ofNullable(rec.date())
                        ));

        // Return only the grouped lists
        return new ArrayList<>(grouped.values());
    }


    /**
     * For each other service point: Count how many deliveries are closer to it
     * than to the current one. And select the one with the highest count (or
     * largest average distance advantage).
     * @param medDispatchRecs list of dispatches that are too large which
     *                        violated one of maxMove or maxCost
     * @param bestServicePoint the best service point that was chosen earlier
     *                        with the list of medDIspatchRecs
     * @return splited dispatches
     */

    private List<List<MedDispatchRec>> splitByAlternativeServicePoint(
            List<MedDispatchRec> medDispatchRecs,
            Position bestServicePoint,
            List<DroneServicePoint> servicePoints
    ){
        if (medDispatchRecs.size()<2)
            throw new IllegalArgumentException("Cannot split, request can't " +
                    "be fulfilled");

        DroneServicePoint altSP = null;
        int maxCount = 0;

        for (DroneServicePoint sp : servicePoints) {
            if (sp.location().equals(bestServicePoint)) continue;

            int closerCount = 0;
            for (MedDispatchRec rec : medDispatchRecs) {
                double distFromBestSP = geometryService.euclidianDistance(
                        rec.delivery(), bestServicePoint);
                double distFromOtherSP = geometryService.euclidianDistance(
                        rec.delivery(), sp.location());
                if (distFromOtherSP < distFromBestSP) closerCount++;
            }

            if (closerCount > maxCount) {
                maxCount = closerCount;
                altSP = sp;
            }
        }

        // If no alternative SP found, fall back and just split into two
        if (altSP == null) {
            return splitIfTooLarge(medDispatchRecs);
        }

        // Partition based on which SP each delivery is closer to
        List<MedDispatchRec> groupA = new ArrayList<>();
        List<MedDispatchRec> groupB = new ArrayList<>();

        for (MedDispatchRec rec : medDispatchRecs) {
            double dBest = geometryService.euclidianDistance(
                    rec.delivery(), bestServicePoint);
            double dOther = geometryService.euclidianDistance(
                    rec.delivery(), altSP.location());
            if (dBest < dOther) groupA.add(rec);
            else groupB.add(rec);
        }

        // Avoid empty groups
        if (groupA.isEmpty() || groupB.isEmpty()) {
            return splitIfTooLarge(medDispatchRecs);
        }

        return List.of(groupA, groupB);
    }


    private List<List<MedDispatchRec>> splitByTemperature(
            List<MedDispatchRec> medDispatchRecs) {
        List<MedDispatchRec> heat = new ArrayList<>();
        List<MedDispatchRec> cool = new ArrayList<>();
        List<MedDispatchRec> neutral = new ArrayList<>();

        for (MedDispatchRec r : medDispatchRecs) {
            if (r.requirements().heating()) heat.add(r);
            else if (r.requirements().cooling()) cool.add(r);
            else neutral.add(r);
        }

        if (heat.isEmpty() && cool.isEmpty()) return
                List.of(medDispatchRecs); // nothing to split

        // Compute midpoints (fallback to centroid of all if group empty)
        Position heatMid = heat.isEmpty() ?
                centroid(medDispatchRecs) : centroid(heat);
        Position coolMid = cool.isEmpty() ?
                centroid(medDispatchRecs) : centroid(cool);

        for (MedDispatchRec r : neutral) {
            double dh = geometryService.
                    euclidianDistance(r.delivery(), heatMid);
            double dc = geometryService.
                    euclidianDistance(r.delivery(), coolMid);
            if (cool.isEmpty() || (!heat.isEmpty() && dh <= dc))
                heat.add(r);
            else cool.add(r);
        }

        List<List<MedDispatchRec>> groups = new ArrayList<>();
        if (!heat.isEmpty()) groups.add(heat);
        if (!cool.isEmpty()) groups.add(cool);
        return groups;
    }

    private Position centroid(List<MedDispatchRec> list) {
        if (list.isEmpty()) return new Position(0.,0.);
        double sx=0, sy=0;
        for (MedDispatchRec r: list) {
            sx += r.delivery().lng();
            sy += r.delivery().lat();
        }
        return new Position(sx/list.size(), sy/list.size());
    }



    private int estimateTotalMove(List<DeliveryTarget> deliveryTargets){
        int cost = 0;
        for (int i = 0; i < deliveryTargets.size()-1; i++){
            cost += moveCostEstimator.estimateMove(
                    deliveryTargets.get(i).delivery(),
                    deliveryTargets.get(i+1).delivery());
        }
        // drone return
        cost += moveCostEstimator.estimateMove(deliveryTargets.getLast().delivery(),
                deliveryTargets.getFirst().delivery());
        return cost;
    }

    public DeliveryWTotalMove planFlightPathSingleDrone(
            List<DeliveryTarget> deliveryRoute,
            List<Region> restrictedRegion)
    {
        List<Delivery> deliveries = new ArrayList<>();
        int totalMove = 0;

        for (int i = 0; i < deliveryRoute.size()-1; i++) {
            Position start = deliveryRoute.get(i).delivery();
            Position end = deliveryRoute.get(i + 1).delivery();

            // Use A* to find path between each point of deliveryRoute
            PositionsWTotalMove flightPathWMove = planPath(
                    start, end, restrictedRegion
            );
            List<Position> flightPath = flightPathWMove.positions();
            totalMove += flightPathWMove.totalMove();

            // Add hover to deliver
            flightPath.add(flightPath.getLast());
            totalMove += 1;

            // MedDispatchRec id is in the delivery target of the dest
            int deliveryId = deliveryRoute.get(i + 1).id();
            deliveries.add(new Delivery(deliveryId, flightPath));
        }

        // return to servicePoint
        PositionsWTotalMove returnFlightWMove =
                planPath(
                        deliveryRoute.getLast().delivery(),
                        deliveryRoute.getFirst().delivery(),
                        restrictedRegion);
        List<Position> returnPath = returnFlightWMove.positions();
        totalMove += returnFlightWMove.totalMove();

        deliveries.add(new Delivery(-1, returnPath));

        return new DeliveryWTotalMove(totalMove, deliveries);
    }

    /**
     * Given the starting point, and a list of positions it needs to make,
     * return the delivery path
     * @param startPos service point the drone start
     * @param deliveries list of positions the drone need to go to
     * @return delivery path
     */
    public List<DeliveryTarget> TSPGreedyNN (Position startPos,
                                             List<DeliveryTarget> deliveries){
        int n = deliveries.size();
        if (n == 0) throw new IllegalArgumentException("No positions found");

        List<DeliveryTarget> deliveryOrder = new ArrayList<>();
        boolean[] visited = new boolean[n]; // not including startPos
        deliveryOrder.add(new DeliveryTarget(-1, startPos));
        for (int i=0; i<n; i++){ // not including startPos
            Position lastVisited = deliveryOrder.get(i).delivery();
            double minDistance =  Double.MAX_VALUE;
            int minIndex=-1;
            for  (int j= 0; j<deliveries.size(); j++){
                if (!visited[j]){
                    Position cur = deliveries.get(j).delivery();
                    double distance  =
                            geometryService.euclidianDistance(lastVisited, cur);
                    if (distance < minDistance){
                        minDistance = distance;
                        minIndex = j;
                    }
                }
            }
            visited[minIndex] = true;
            deliveryOrder.add(deliveries.get(minIndex));
        }
        return deliveryOrder;
    }
    @Override
    public GeoJsonFeatureCollection calcDeliveryPathAsGeoJson (
            List<MedDispatchRec> medDispatchRecs)
    {
        var ilpSnapshot = ilpRestClient.getIlpServiceSnapshot();
        List<DroneServicePoint> servicePoints = ilpSnapshot.servicePoints();
        List<Drone> drones = ilpSnapshot.drones();
        Map<String, Drone> idToDrone = drones.stream().collect(Collectors.toMap(
                Drone::id,
                d -> d
        ));
        List<Region> restrictedRegion = ilpSnapshot.restrictedRegions();
        List<DroneForServicePoint> droneForServicePoints =
                ilpSnapshot.dronesForServicePoints();

        List<GeoJsonFeature> featureList = new ArrayList<>();

        // Assume one drone
        DeliveryPath deliveryPaths = calcDeliveryPath(medDispatchRecs);


//        if (deliveryPaths.size() != 1)
//            throw new IllegalArgumentException(
//                    "Delivery paths must have exactly one drone");

        // Convert dronePath to LineString
        for (DronePath dronePath: deliveryPaths.dronePaths()){
            GeoJsonFeature droneLineString =
                    dronePathToLineStringFeature(dronePath);
            featureList.add(droneLineString);
        }

        // Convert restricted Region into polygons
        for (Region r : restrictedRegion){
            GeoJsonFeature polygonFeature = regionToPolygonFeature(r);
            featureList.add(polygonFeature);
        }

        // Convert medDispatchRecs delivery point to points
        for  (MedDispatchRec medDispatchRec : medDispatchRecs){
            GeoJsonFeature point = medDispatchRecToPointFeature(medDispatchRec);
            featureList.add(point);
        }

        // Convert servicePoint to points
        for (DroneServicePoint servicePoint : servicePoints){
            GeoJsonFeature point = servicePointToPointFeature(servicePoint);
            featureList.add(point);
        }

        return new GeoJsonFeatureCollection(featureList);
    }

    private GeoJsonFeature dronePathToLineStringFeature(
            DronePath dronePath){

        List<List<Double>> coordinates = new ArrayList<>();

        // should only iterate once since we assume one drone
        for (Delivery delivery: dronePath.deliveries()){
            List<Position> flightPath = delivery.flightPath();
            coordinates.addAll(positionsToCoordinate(flightPath));
        }

        LineString deliveryPath = new LineString(coordinates);
        return new GeoJsonFeature(deliveryPath,
                Map.of("droneId", dronePath.droneId()));
    }

    private GeoJsonFeature regionToPolygonFeature (Region region){
        List<List<Double>> coordinates = positionsToCoordinate(region.vertices());
        Polygon polygon = new Polygon(List.of(coordinates));
        return new GeoJsonFeature(polygon, Map.of("name", region.name()));
    }

    private List<List<Double>> positionsToCoordinate(List<Position> positions)
    {
        List<List<Double>> coordinates = new ArrayList<>();
        for (Position position: positions){
            coordinates.add(List.of(position.lng(), position.lat()));
        }
        return coordinates;
    }

    private GeoJsonFeature servicePointToPointFeature(
            DroneServicePoint servicePoint)
    {
        GeoJsonFeature point = positionToPointFeature(servicePoint.location());
        Map<String, Object> properties = point.getProperties();
        properties.put("id", servicePoint.id());
        properties.put("name", servicePoint.name());
        properties.put("marker-color", "#FE6100"); // orange
        return point;
    }

    private GeoJsonFeature medDispatchRecToPointFeature(
            MedDispatchRec medDispatchRec)
    {
        GeoJsonFeature point = positionToPointFeature(medDispatchRec.delivery());
        Map<String, Object> properties = point.getProperties();
        properties.put("id", medDispatchRec.id());
        properties.put("marker-color", "#648FFF"); // blue
        return point;
    }

    private GeoJsonFeature positionToPointFeature(Position position)
    {
        List<Double> coordinate = List.of(position.lng(), position.lat());
        Point point = new Point(coordinate);
        return new GeoJsonFeature(point, new HashMap<>());
    }

}

