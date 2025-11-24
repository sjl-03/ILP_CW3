package com.ilp.cw3.drone_simulator.client;

import com.ilp.cw3.drone_simulator.model.DeliveryPath;
import com.ilp.cw3.drone_simulator.model.Drone;
import com.ilp.cw3.drone_simulator.model.MedDispatchRec;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class CW2Client {
    private final WebClient webClient;

    public CW2Client(WebClient webClient) {
        this.webClient = webClient;
    }

    public DeliveryPath calcDeliveryPath(List<MedDispatchRec> medDispatchRecs) {
        return webClient
                .post()
                .uri("/api/v1/calcDeliveryPath")
                .bodyValue(medDispatchRecs)
                .retrieve()
                .bodyToMono(DeliveryPath.class)
                .block();
    }

    public Drone getDroneDetails(String droneId) {
        return webClient
                .get()
                .uri("/api/v1/droneDetails/{droneId}", droneId)
                .retrieve()
                .bodyToMono(Drone.class)
                .block();
    }
}