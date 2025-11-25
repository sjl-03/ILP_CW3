package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ed.acp.cw2.client.IlpRestClient;
import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.Region;
import uk.ac.ed.acp.cw2.dto.DeliveryTarget;
import uk.ac.ed.acp.cw2.services.*;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;

public class PathPlanningTests {
    final IlpRestClient ilpRestClient = Mockito.mock(IlpRestClient.class);
    final GeometryService geometryService = new GeometryServiceImpl();
    final DroneQueriesService droneQueriesService =
            new DroneQueriesServiceImpl(ilpRestClient);
    PathPlanningServiceImpl pathPlanningService  =
            new PathPlanningServiceImpl(ilpRestClient,geometryService,
                    droneQueriesService);

    @Test
    void testTSPGreedyNN(){



        Position startPos = new Position(3.0,4.0);
        List<DeliveryTarget> positions = Arrays.asList(
                new DeliveryTarget(1, new Position(2.0, 2.0)),
                new DeliveryTarget(2,new Position(4.0, 3.0)),
                new DeliveryTarget(3,new Position(4.0, 5.0)),
                new DeliveryTarget(4,new Position(7.0, 6.0)),
                new DeliveryTarget(5,new Position(8.0, 8.0)));
        List<DeliveryTarget> result = pathPlanningService.TSPGreedyNN(startPos,
                positions);

        for (DeliveryTarget p : result) {
            System.out.println(p);
        }
    }

    @Test
    void testGetNeighbours() {
        Position current = new Position(0.0, 0.0);
        List<Position> restrictedRegion1Point = Arrays.asList(
                new Position(0.0001, 1.0),
                new Position(0.00005, -2.0),
                new Position(1.00005, -2.0),
                new Position(0.0001, 1.0)
        );
        Region region1 = new Region("triangle", restrictedRegion1Point);
        List<Position> restrictedRegion2Point = Arrays.asList(
                new Position(-0.00001, 1.0),
                new Position(-1.0, 1.0),
                new Position(-1.0, -1.0),
                new Position(-0.00001, -1.0),
                new Position(-0.00001, 1.0)
        );
        Region region2 = new Region("rectangle", restrictedRegion2Point);




        List<Position> result = pathPlanningService
                .getNeighbours(current,Arrays.asList(region1,region2));

        for (Position p : result) {
            System.out.println(p);
        }
        // Expect 4 output
    }
}
