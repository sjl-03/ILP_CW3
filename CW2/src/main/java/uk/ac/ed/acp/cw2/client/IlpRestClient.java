package uk.ac.ed.acp.cw2.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.acp.cw2.data.Drone;
import uk.ac.ed.acp.cw2.data.DroneForServicePoint;
import uk.ac.ed.acp.cw2.data.DroneServicePoint;
import uk.ac.ed.acp.cw2.data.Region;

import java.util.List;
@Component
public class IlpRestClient {
    private final WebClient webClient;

    public IlpRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Drone> getDrones() {
        return webClient
                .get()
                .uri("/drones")
                .retrieve()
                .bodyToFlux(Drone.class)
                .collectList()
                .block();
    }

    public List<DroneForServicePoint> getDronesforServicePoints(){
        return webClient
                .get()
                .uri("/drones-for-service-points")
                .retrieve()
                .bodyToFlux(DroneForServicePoint.class)
                .collectList()
                .block();
    }

    public List<DroneServicePoint> getDroneServicePoint(){
        return webClient
                .get()
                .uri("/service-points")
                .retrieve()
                .bodyToFlux(DroneServicePoint.class)
                .collectList()
                .block();
    }

    public List<Region> getRestrictedRegion() {
        return webClient
                .get()
                .uri("/restricted-areas")
                .retrieve()
                .bodyToFlux(Region.class)
                .collectList()
                .block();
    }

    public static record IlpServiceSnapshot(
            List<Drone> drones,
            List<DroneForServicePoint> dronesForServicePoints,
            List<DroneServicePoint> servicePoints,
            List<Region> restrictedRegions
    ) {}

    public IlpServiceSnapshot getIlpServiceSnapshot() {
        List<Drone> drones = getDrones();
        List<DroneForServicePoint> dronesForSP = getDronesforServicePoints();
        List<DroneServicePoint> sps = getDroneServicePoint();
        List<Region> regions = getRestrictedRegion();
        return new IlpServiceSnapshot(drones, dronesForSP, sps, regions);
    }
}
