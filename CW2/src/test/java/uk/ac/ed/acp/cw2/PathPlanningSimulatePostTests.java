package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.*;
import uk.ac.ed.acp.cw2.data.geoJson.GeoJsonFeatureCollection;
import uk.ac.ed.acp.cw2.services.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PathPlanningSimulatePostTests {
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


    List<MedDispatchRec> recsOneDrone = List.of(
            new MedDispatchRec(
                    1,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.75, false, true, 13.5),
                    new Position(-3.189, 55.941)
            ),
            new MedDispatchRec(
                    2,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.15, false, false, 10.5),
                    new Position(-3.189, 55.951)
            ),
            new MedDispatchRec(
                    3,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.85, false, false, 5.0),
                    new Position(-3.183, 55.950)
            ),
            new MedDispatchRec(
                    4,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.65, false, true, 5.0),
                    new Position(-3.213, 55.940)
            )
    );

    List<MedDispatchRec> recsMissingFieldValid = List.of(
            new MedDispatchRec(
                    4,
                    null,
                    null,
                    new Requirements(
                            3.0,       // capacity
                            true,      // cooling
                            false,      // missing boolean field
                            null       // missing maxCost
                    ),
                    new Position(-3.187500, 55.945500)
            ),
            new MedDispatchRec(
                    5,
                    null,
                    null,
                    new Requirements(
                            1.5,       // capacity
                            true,      // cooling
                            false,      // missing boolean field
                            null       // missing maxCost
                    ),
                    new Position(-3.189000, 55.946000)
            )
    );


    List<MedDispatchRec> diffDate = List.of(
            new MedDispatchRec(
                    1,
                    java.time.LocalDate.parse("2025-12-23"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.75, false, true, 13.5),
                    new Position(-3.189, 55.941)
            ),
            new MedDispatchRec(
                    2,
                    java.time.LocalDate.parse("2025-12-23"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.15, false, false, 10.5),
                    new Position(-3.189, 55.951)
            ),
            new MedDispatchRec(
                    3,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(6., false, false, 15.0),
                    new Position(-3.183, 55.950)
            ),
            new MedDispatchRec(
                    4,
                    java.time.LocalDate.parse("2025-12-23"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.65, false, true, 5.0),
                    new Position(-3.213, 55.940)
            )
    );

    List<MedDispatchRec> need2drone = List.of(
            new MedDispatchRec(
                    1,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.75, false, true, 13.5),
                    new Position(-3.189, 55.941)
            ),
            new MedDispatchRec(
                    2,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.15, false, false, 10.5),
                    new Position(-3.189, 55.951)
            ),
            new MedDispatchRec(
                    3,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(6.0, false, false, 5.0),
                    new Position(-3.183, 55.950)
            ),
            new MedDispatchRec(
                    4,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.65, false, true, 5.0),
                    new Position(-3.213, 55.940)
            ),
            new MedDispatchRec(
                    5,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.75, false, true, 13.5),
                    new Position(-3.2088, 55.9799)
            ),
            new MedDispatchRec(
                    6,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.15, false, false, 15.5),
                    new Position(-3.1845, 55.9707)
            )
    );

    List<MedDispatchRec> differentDroneForAvailableAndUsedInvalid = List.of(
            new MedDispatchRec(
                    1,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.75, false, true, 13.5),
                    new Position(-3.189, 55.941)
            ),
            new MedDispatchRec(
                    2,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.15, false, false, 10.5),
                    new Position(-3.189, 55.951)
            ),
            new MedDispatchRec(
                    3,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(6.0, false, false, 5.0),
                    new Position(-3.183, 55.950)
            ),
            new MedDispatchRec(
                    4,
                    java.time.LocalDate.parse("2025-12-22"),
                    java.time.LocalTime.parse("14:30"),
                    new Requirements(0.65, false, true, 5.0),
                    new Position(-3.213, 55.940)
            )
    );

    // -------------Restricted Area----------------
    List<Region> givenRestricted = List.of(
            new Region("George Square Area", List.of(
                    new Position(-3.190578818321228, 55.94402412577528),
                    new Position(-3.1899887323379517, 55.94284650540911),
                    new Position(-3.187097311019897, 55.94328811724263),
                    new Position(-3.187682032585144, 55.944477740393744),
                    new Position(-3.190578818321228, 55.94402412577528)
            )),
            new Region("Dr Elsie Inglis Quadrangle", List.of(
                    new Position(-3.1907182931900024, 55.94519570234043),
                    new Position(-3.1906163692474365, 55.94498241796357),
                    new Position(-3.1900262832641597, 55.94507554227258),
                    new Position(-3.190133571624756, 55.94529783810495),
                    new Position(-3.1907182931900024, 55.94519570234043)
            )),
            new Region("Bristol Square Open Area", List.of(
                    new Position(-3.189543485641479, 55.94552313663306),
                    new Position(-3.189382553100586, 55.94553214854692),
                    new Position(-3.189259171485901, 55.94544803726933),
                    new Position(-3.1892001628875732, 55.94533688994374),
                    new Position(-3.189194798469543, 55.94519570234043),
                    new Position(-3.189135789871216, 55.94511759833873),
                    new Position(-3.188138008117676, 55.9452738061846),
                    new Position(-3.1885510683059692, 55.946105902745614),
                    new Position(-3.1895381212234497, 55.94555918427592),
                    new Position(-3.189543485641479, 55.94552313663306)
            )),
            new Region("Bayes Central Area", List.of(
                    new Position(-3.1876927614212036, 55.94520696732767),
                    new Position(-3.187555968761444, 55.9449621408666),
                    new Position(-3.186981976032257, 55.94505676722831),
                    new Position(-3.1872327625751495, 55.94536993377657),
                    new Position(-3.1874459981918335, 55.9453361389472),
                    new Position(-3.1873735785484314, 55.94519344934259),
                    new Position(-3.1875935196876526, 55.94515665035927),
                    new Position(-3.187624365091324, 55.94521973430925),
                    new Position(-3.1876927614212036, 55.94520696732767)
            ))
    );

    // -----------------Service Point----------------
    List<DroneServicePoint> givenSP = List.of(
            new DroneServicePoint("Appleton Tower", 1,
                    new Position(-3.1863580788986368, 55.94468066708487)),
            new DroneServicePoint("Ocean Terminal", 2,
                    new Position(-3.17732611501824, 55.981186279333656))
    );

    // -----------------Drones---------------------
    List<Drone> givenDrones = List.of(
            new Drone("Drone 1", "1",  new Capabilities(true,  true,  4.0, 2000, 0.01, 4.3, 6.5)),
            new Drone("Drone 2", "2",  new Capabilities(false, true,  8.0, 1000, 0.03, 2.6, 5.4)),
            new Drone("Drone 3", "3",  new Capabilities(false, false, 20.0, 4000, 0.05, 9.5, 11.5)),
            new Drone("Drone 4", "4",  new Capabilities(false, true,  8.0, 1000, 0.02, 1.4, 2.5)),
            new Drone("Drone 5", "5",  new Capabilities(true,  true,  12.0, 1500, 0.04, 1.8, 3.5)),
            new Drone("Drone 6", "6",  new Capabilities(false, true,  4.0, 2000, 0.03, 3.0, 4.0)),
            new Drone("Drone 7", "7",  new Capabilities(false, true,  8.0, 1000, 0.015, 1.4, 2.2)),
            new Drone("Drone 8", "8",  new Capabilities(true,  false, 20.0, 4000, 0.04, 5.4, 12.5)),
            new Drone("Drone 9", "9",  new Capabilities(true,  true,  8.0, 1000, 0.06, 2.4, 1.5)),
            new Drone("Drone 10", "10", new Capabilities(false, false, 12.0, 1500, 0.07, 1.4, 3.5))
    );

    // ------------------Drone for service point------------------
    Availability mondayAllDay = new Availability("MONDAY",
            java.time.LocalTime.parse("00:00:00"),
            java.time.LocalTime.parse("23:59:59"));

    DroneForServicePoint dfsp1 = new DroneForServicePoint(1, List.of(
            new DronesAvailability("1",  List.of(mondayAllDay,
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("2",  List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("3",  List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("4",  List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("5",  List.of(
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59"))
            ))
    ));

    DroneForServicePoint dfsp2 = new DroneForServicePoint(2, List.of(
            new DronesAvailability("6",  List.of(mondayAllDay,
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("7",  List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("8",  List.of(
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("9",  List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("11:59:59")),
                    new Availability("WEDNESDAY", java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("THURSDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SATURDAY",  java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("SUNDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            )),
            new DronesAvailability("10", List.of(
                    new Availability("MONDAY",    java.time.LocalTime.parse("12:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("TUESDAY",   java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59")),
                    new Availability("FRIDAY",    java.time.LocalTime.parse("00:00:00"), java.time.LocalTime.parse("23:59:59"))
            ))
    ));
    List<DroneForServicePoint> givenDroneForServicePoints =
            List.of(dfsp1,dfsp2);

    @Test
    void testCalcDeliveryPath() {
        // ---------- Arrange ----------
        // Mock client + services
        IlpRestClient restClient = Mockito.mock(IlpRestClient.class);
        GeometryService geometry = new GeometryServiceImpl();
        DroneQueriesService droneService = new DroneQueriesServiceImpl(restClient);
        PathPlanningServiceImpl planner = new PathPlanningServiceImpl(restClient, geometry, droneService);

        // 1) Choose MedDispatchRecs (recsOneDrone, need2drone, diffDate,
        // recsMissingFieldValid)
        List<MedDispatchRec> medDispatchRecs = recsOneDrone;

        // 2) Restricted regions (all polygons are closed: first==last)
        when(restClient.getRestrictedRegion()).thenReturn(givenRestricted);

        // 3) Service points
        when(restClient.getDroneServicePoint()).thenReturn(givenSP);

        // 4) Drones (capabilities). Note: Drone.id likely int; names as given
        when(restClient.getDrones()).thenReturn(givenDrones);

        // 5) Drones for service points (IDs are String here; availability covers MONDAY)
        // Note: 2025-12-22 is a Monday
        when(restClient.getDronesforServicePoints()).
                thenReturn(givenDroneForServicePoints);

        IlpRestClient.IlpServiceSnapshot ilpSnapshot =
                new IlpRestClient.IlpServiceSnapshot(
                        givenDrones, givenDroneForServicePoints,
                        givenSP, givenRestricted
                );
        when(restClient.getIlpServiceSnapshot()).thenReturn(ilpSnapshot);

        // ---------- Act ----------
        DeliveryPath result = planner.calcDeliveryPath(medDispatchRecs);

        System.out.println("Total Cost : " + result.totalCost());
        System.out.println("Total Moves: " + result.totalMoves());
        for (DronePath dp : result.dronePaths()) {
            System.out.println("Drone " + dp.droneId() + " path:");
            for (Delivery d : dp.deliveries()) {
                System.out.println("  deliveryId=" + d.deliveryId() + " pathLen=" + d.flightPath().size());
            }
        }

        // ---------- Assert ----------
        assertNotNull(result, "Result should not be null");
        assertFalse(result.dronePaths().isEmpty(), "Should allocate at least one drone");
        assertTrue(result.totalMoves() > 0, "Total moves should be positive");

        // Optional: if your splitting is active, you might expect >=1 or >1 paths.
        // assertTrue(result.dronePaths().size() >= 1);

        // Optional sanity check: ensure all deliveries are covered at least once
        Set<Integer> deliveredIds = result.dronePaths().stream()
                .flatMap(dp -> dp.deliveries().stream())
                .map(Delivery::deliveryId)
                .filter(id -> id != -1) // ignore return-to-SP
                .collect(java.util.stream.Collectors.toSet());
    }

//    @Test
//    void testCalcDeliveryPathAsGeoJson() {
//        // ---------- Arrange ----------
//        // Mock client + services
//        IlpRestClient restClient = Mockito.mock(IlpRestClient.class);
//        GeometryService geometry = new GeometryServiceImpl();
//        DroneQueriesService droneService = new DroneQueriesServiceImpl(restClient);
//        PathPlanningServiceImpl planner = new PathPlanningServiceImpl(restClient, geometry, droneService);
//
//        // 1) Choose MedDispatchRecs (recsOneDrone, need2drone, diffDate)
//        List<MedDispatchRec> medDispatchRecs = need2drone;
//
//        // 2) Restricted regions (all polygons are closed: first==last)
//        List<Region> restricted = givenRestricted;
//        when(restClient.getRestrictedRegion()).thenReturn(restricted);
//
//        // 3) Service points
//
//        when(restClient.getDroneServicePoint()).thenReturn(givenSP);
//
//        // 4) Drones (capabilities). Note: Drone.id likely int; names as given
//        List<Drone> drones = givenDrones;
//        when(restClient.getDrones()).thenReturn(drones);
//
//        // 5) Drones for service points (IDs are String here; availability covers MONDAY)
//        // Note: 2025-12-22 is a Monday
//        when(restClient.getDronesforServicePoints()).
//                thenReturn(givenDroneForServicePoints);
//
//        IlpRestClient.IlpServiceSnapshot ilpSnapshot =
//                new IlpRestClient.IlpServiceSnapshot(
//                        givenDrones, givenDroneForServicePoints,
//                        givenSP, givenRestricted
//                );
//        when(restClient.getIlpServiceSnapshot()).thenReturn(ilpSnapshot);
//
//        // ---------- Act ----------
//        GeoJsonFeatureCollection result =
//                planner.calcDeliveryPathAsGeoJson(medDispatchRecs);
//
//        // ---------- Assert ----------
//        System.out.println(
//                result
//        );
//    }
}
