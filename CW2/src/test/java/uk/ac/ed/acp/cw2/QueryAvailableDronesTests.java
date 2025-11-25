package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.*;
import uk.ac.ed.acp.cw2.services.PathPlanningServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class QueryAvailableDronesTests {
    private IlpRestClient restClient;
    private PathPlanningServiceImpl planner;

    @BeforeEach
    void setup() {
        restClient = Mockito.mock(IlpRestClient.class);
        // GeometryServiceImpl and DroneQueriesServiceImpl used in constructor; reuse simple real ones
        var geometry = new uk.ac.ed.acp.cw2.services.GeometryServiceImpl();
        var droneQueries = new uk.ac.ed.acp.cw2.services.DroneQueriesServiceImpl(restClient);
        planner = new PathPlanningServiceImpl(restClient, geometry, droneQueries);
    }

    @Test
    void emptyInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> planner.queryAvailableDrones(List.of()));
    }

    @Test
    void singleDispatchMatchesDrone() {
        // one dispatch on Monday 09:00; drone "D1" available MONDAY all day at SP 1
        var d1 = new Drone("D1", "D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var mondayAllDay = new Availability("MONDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var dfsp = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(mondayAllDay))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp), List.of(sp1), List.of())
        );

        var rec = new MedDispatchRec(1, LocalDate.parse("2025-12-22"), LocalTime.parse("09:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.));
        var res = planner.queryAvailableDrones(List.of(rec));
        assertEquals(1, res.size());
        assertTrue(res.contains("D1"));
    }

    @Test
    void sameDayDifferentServicePoints_returnsDroneIfAvailable() {
        // Drone D1 is available at SP1 and SP2 on MONDAY; two dispatches same date different SPs -> D1 should be returned
        var d1 = new Drone("D1","D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var sp2 = new DroneServicePoint("SP2", 2, new Position(1.,1.));
        var mondayAllDay = new Availability("MONDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var dfsp1 = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(mondayAllDay))));
        var dfsp2 = new DroneForServicePoint(2, List.of(new DronesAvailability("D1", List.of(mondayAllDay))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp1, dfsp2), List.of(sp1,sp2), List.of())
        );

        var rec1 = new MedDispatchRec(1, LocalDate.parse("2025-12-22"), LocalTime.parse("09:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.));
        var rec2 = new MedDispatchRec(2, LocalDate.parse("2025-12-22"), LocalTime.parse("14:00"),
                new Requirements(1.0,false,false,null), new Position(1.,1.));
        var res = planner.queryAvailableDrones(List.of(rec1, rec2));
        assertEquals(1, res.size());
        assertTrue(res.contains("D1"));
    }

    @Test
    void differentDates_droneMustBeAvailableOnAllDates() {
        // D1 available MONDAY and TUESDAY -> should be returned for dispatches spanning both days
        var d1 = new Drone("D1","D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var monday = new Availability("MONDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var tuesday = new Availability("TUESDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var dfsp = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(monday, tuesday))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp), List.of(sp1), List.of())
        );

        var recMon = new MedDispatchRec(1, LocalDate.parse("2025-12-22"), LocalTime.parse("09:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.)); // Monday
        var recTue = new MedDispatchRec(2, LocalDate.parse("2025-12-23"), LocalTime.parse("10:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.)); // Tuesday
        var res = planner.queryAvailableDrones(List.of(recMon, recTue));
        assertEquals(1, res.size());
        assertTrue(res.contains("D1"));
    }

    @Test
    void differentDates_missingOneDateAvailability_returnsEmpty() {
        // D1 available only MONDAY; requests include a TUESDAY -> should return empty
        var d1 = new Drone("D1","D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var monday = new Availability("MONDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var dfsp = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(monday))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp), List.of(sp1), List.of())
        );

        var recMon = new MedDispatchRec(1, LocalDate.parse("2025-12-22"), LocalTime.parse("09:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.)); // Monday
        var recTue = new MedDispatchRec(2, LocalDate.parse("2025-12-23"), LocalTime.parse("10:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.)); // Tuesday
        var res = planner.queryAvailableDrones(List.of(recMon, recTue));
        assertTrue(res.isEmpty());
    }

    @Test
    void nullDateActsAsNoConstraint() {
        // D1 available MONDAY; one dispatch with null date + one on MONDAY -> D1 should be OK
        var d1 = new Drone("D1","D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var monday = new Availability("MONDAY", LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"));
        var dfsp = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(monday))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp), List.of(sp1), List.of())
        );

        var recNull = new MedDispatchRec(1, null, null,
                new Requirements(1.0,false,false,null), new Position(0.,0.));
        var recMon = new MedDispatchRec(2, LocalDate.parse("2025-12-22"), LocalTime.parse("11:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.));
        var res = planner.queryAvailableDrones(List.of(recNull, recMon));
        assertEquals(1, res.size());
        assertTrue(res.contains("D1"));
    }

    @Test
    void timeWindowExcludesDrone() {
        // D1 available MONDAY 08:00-09:00; dispatch at 10:00 -> should be excluded
        var d1 = new Drone("D1","D1", new Capabilities(false,false,10,1000,0.0,1.0,1.0));
        var sp1 = new DroneServicePoint("SP1", 1, new Position(0.,0.));
        var avail = new Availability("MONDAY", LocalTime.parse("08:00:00"), LocalTime.parse("09:00:00"));
        var dfsp = new DroneForServicePoint(1, List.of(new DronesAvailability("D1", List.of(avail))));
        when(restClient.getIlpServiceSnapshot()).thenReturn(
                new IlpRestClient.IlpServiceSnapshot(List.of(d1), List.of(dfsp), List.of(sp1), List.of())
        );

        var rec = new MedDispatchRec(1, LocalDate.parse("2025-12-22"), LocalTime.parse("10:00"),
                new Requirements(1.0,false,false,null), new Position(0.,0.));
        var res = planner.queryAvailableDrones(List.of(rec));
        assertTrue(res.isEmpty());
    }
}