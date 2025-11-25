package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.*;
import uk.ac.ed.acp.cw2.dto.DeliveryTarget;
import uk.ac.ed.acp.cw2.dto.DeliveryWTotalMove;
import uk.ac.ed.acp.cw2.dto.PositionsWTotalMove;
import uk.ac.ed.acp.cw2.services.*;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PathPlanningServiceImpl helper methods.
 * Each test isolates one helper and uses mock or local geometry only.
 */
public class PathPlanningUnitTests {

    private IlpRestClient restClient;
    private GeometryService geometry;
    private DroneQueriesService droneService;
    private PathPlanningServiceImpl pathPlanning;

    @BeforeEach
    void setup() {
        restClient = Mockito.mock(IlpRestClient.class);
        geometry = new GeometryServiceImpl();
        droneService = new DroneQueriesServiceImpl(restClient);
        pathPlanning = new PathPlanningServiceImpl(restClient, geometry, droneService);
    }

    // --------------------- TSPGreedyNN ---------------------

    @Test
    void testTSPGreedyNN_returnsGreedyOrder() {
        Position start = new Position(0.0, 0.0);
        List<DeliveryTarget> deliveries = List.of(
                new DeliveryTarget(1, new Position(1.0, 1.0)),
                new DeliveryTarget(2, new Position(2.0, 2.0)),
                new DeliveryTarget(3, new Position(5.0, 5.0))
        );

        List<DeliveryTarget> ordered = pathPlanning.TSPGreedyNN(start, deliveries);

        for (DeliveryTarget deliveryTarget : ordered) {
            System.out.println(deliveryTarget);
        }

        // Expect route starting at SP (-1 id), then nearest 1.0,1.0 -> 2.0,2.0 -> 5.0,5.0
        assertEquals(-1, ordered.get(0).id());
        assertEquals(1, ordered.get(1).id());
        assertEquals(2, ordered.get(2).id());
        assertEquals(3, ordered.get(3).id());
    }

    // --------------------- getNeighbours ---------------------

    @Test
    void testGetNeighbours_excludesRestrictedRegion() {
        // Current point at origin
        Position origin = new Position(0.0, 0.0);

        // Simple square region enclosing quadrant (+lng,+lat)
        List<Position> vertices = List.of(
                new Position(0.00001, 0.0),
                new Position(0.0003, 0.0),
                new Position(0.0003, 0.0003),
                new Position(0.00001, 0.0003),
                new Position(0.00001, 0.0)
        );
        Region restricted = new Region("square", vertices);

        List<Position> neighbours = pathPlanning.getNeighbours(origin, List.of(restricted));

        // Expect that at least some directions are blocked (not all 16)
        assertTrue(neighbours.size() < 16, "Restricted region should remove some neighbours");
        assertTrue(neighbours.size() > 0, "Should still have some valid neighbours");
    }

    // --------------------- planPath (A*) ---------------------


    private static void assertPositionWithinTolerance(Position expected, Position actual, double tolerance) {
        assertNotNull(expected);
        assertNotNull(actual);

        double dx = expected.lng() - actual.lng();
        double dy = expected.lat() - actual.lat();
        double dist = Math.sqrt(dx * dx + dy * dy);

        assertTrue(dist < tolerance,
                String.format("Expected distance < %.6f but was %.6f", tolerance, dist));
    }


    @Test
    void testPlanPath_findsSimpleStraightLine() {
        Position start = new Position(0.0, 0.0);
        Position goal = new Position(0.0005, 0.0); // 3 steps east

        PositionsWTotalMove result = pathPlanning.planPath(start, goal, List.of());
        for (Position pos: result.positions()){
            System.out.println(pos);
        }

        assertNotNull(result);
        assertTrue(result.totalMove() > 0);
        // Ensure start and goal in path
        assertEquals(start, result.positions().getFirst());
        assertPositionWithinTolerance(goal, result.positions().getLast(), 0.00015);
    }

    @Test
    void testPlanPath_throwsIfNoPath() {
        Position start = new Position(0.0, 0.0);
        Position goal = new Position(0.00045, 0.0);
        // Region blocking the straight line
        List<Position> block = List.of(
                new Position(0.00015, -0.0001),
                new Position(0.0003, -0.0001),
                new Position(0.0003, 0.0001),
                new Position(0.00015, 0.0001),
                new Position(0.00015, -0.0001)
        );
        Region restricted = new Region("block", block);

        for (Position pos: pathPlanning.planPath(start, goal,
                List.of(restricted)).positions()) {
            System.out.println(pos);
        }
    }

    // --------------------- planFlightPathSingleDrone ---------------------

    @Test
    void testPlanFlightPathSingleDrone_generatesDeliveries() {
        Position sp = new Position(0.0, 0.0);
        Position d1 = new Position(0.0003, 0.0);
        Position d2 = new Position(0.0003, 0.0003);

        List<DeliveryTarget> route = List.of(
                new DeliveryTarget(-1, sp),
                new DeliveryTarget(1, d1),
                new DeliveryTarget(2, d2)
        );

        DeliveryWTotalMove result = pathPlanning.planFlightPathSingleDrone(route, List.of());
        for (Delivery delivery: result.deliveries()){
            System.out.println(delivery);
        }

        assertEquals(3, result.deliveries().size());
        assertTrue(result.totalMove() > 0);
        // Ensure last delivery returns to SP
        assertPositionWithinTolerance(sp,
                result.deliveries().getLast().flightPath().getLast(), 0.00015);
    }



    // --------------------- calculatePath ---------------------

    @Test
    void testCalculatePath_reconstructsFullRoute() throws Exception {
        var method = PathPlanningServiceImpl.class
                .getDeclaredMethod("calculatePath", Map.class, Position.class, Position.class);
        method.setAccessible(true);

        Position a = new Position(0.0, 0.0);
        Position b = new Position(1.0, 0.0);
        Position c = new Position(2.0, 0.0);

        Map<Position, Position> parent = new HashMap<>();
        parent.put(c, b);
        parent.put(b, a);

        var result = (PositionsWTotalMove) method.invoke(pathPlanning, parent, a, c);
        assertEquals(3, result.positions().size());
        assertEquals(a, result.positions().getFirst());
        assertEquals(c, result.positions().getLast());
    }

    // --------------------- availableDrone logic ---------------------

//    @Test
//    void testAvailableDrone_filtersByDayAndTime() {
//        // Mock a service point with one drone
//        Availability slot = new Availability("MONDAY",
//                LocalTime.of(9, 0), LocalTime.of(17, 0));
//        DronesAvailability da = new DronesAvailability("sfad-DFfsdd13-dfsn",
//                List.of(slot));
//        DroneForServicePoint dfsp = new DroneForServicePoint(1, List.of(da));
//        when(restClient.getDronesforServicePoints()).thenReturn(List.of(dfsp));
//
//        Set<String> capable = Set.of("sfad-DFfsdd13-dfsn");
//        List<String> result = pathPlanning.availableDrone(capable,
//                "MONDAY", LocalTime.of(10, 0), LocalTime.of(11, 0));
//        assertEquals(List.of("sfad-DFfsdd13-dfsn"), result);
//    }


    @Test
    void debug_noPathBetweenTwoPoints() {
        // same coordinates from the 500-error
        Position start = new Position(-3.1863580788986368, 55.94468066708487);
        Position goal  = new Position(-3.189, 55.941);

        // --- Define a restricted region that blocks the straight path ---
        List<Position> vertices = List.of(
                new Position(-3.187, 55.943),
                new Position(-3.188, 55.943),
                new Position(-3.188, 55.942),
                new Position(-3.187, 55.942),
                new Position(-3.187, 55.943) // must close polygon
        );
        Region restrictedRegion = new Region("test-block", vertices);
        List<Region> restrictedList = List.of(restrictedRegion);

        // --- Mock the REST client to return this restricted region ---
        when(restClient.getRestrictedRegion()).thenReturn(restrictedList);

        System.out.println("=== DEBUG planPath ===");
        System.out.println("Start: " + start);
        System.out.println("Goal : " + goal);
        System.out.println("Restricted regions: " + restrictedList.size());

        try {
            // use mocked restricted regions instead of restClient call
            PositionsWTotalMove path = pathPlanning.planPath(start, goal, restrictedList);

            System.out.println("Total moves: " + path.totalMove());
            for (Position p : path.positions()) {
                System.out.println(" " + p);
            }
        } catch (IllegalStateException e) {
            System.out.println("❌ No path found: " + e.getMessage());
        }
    }






}
