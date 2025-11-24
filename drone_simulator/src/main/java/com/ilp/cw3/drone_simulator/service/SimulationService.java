package com.ilp.cw3.drone_simulator.service;

import com.ilp.cw3.drone_simulator.client.CW2Client;
import com.ilp.cw3.drone_simulator.controller.SimulatorController;
import com.ilp.cw3.drone_simulator.model.*;
import com.ilp.cw3.drone_simulator.rabbitmq.TelemetrySender;
import com.ilp.cw3.drone_simulator.simulation.DroneLiveSimulator;
import com.ilp.cw3.drone_simulator.simulation.PathLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationService {
    private static final Logger logger =
            LoggerFactory.getLogger(SimulationService.class);

    private final CW2Client cw2Client;
    private final DroneLiveSimulator droneLiveSimulator;
    private final PathConversionService pathConversionService;

    public SimulationService(
            CW2Client cw2Client,
            DroneLiveSimulator droneLiveSimulator,
            PathConversionService pathConversionService){
        this.cw2Client = cw2Client;
        this.droneLiveSimulator = droneLiveSimulator;
        this.pathConversionService = pathConversionService;
    }

    // Assume one drone
    public void simulateDeliveryPath (
            List<MedDispatchRec> medDispatchRecs) {

        DeliveryPath deliveryPath;
        DroneDetail droneDetail;
        DronePath dronePath;

        if (medDispatchRecs == null ||  medDispatchRecs.isEmpty()) {
            DroneDeliveryInfo info = loadFallback();
            deliveryPath = info.deliveryPath();
            droneDetail = info.droneDetail();
        }
        else{
            try {
                deliveryPath = cw2Client.calcDeliveryPath(medDispatchRecs);
                 dronePath = deliveryPath.dronePaths().get(0);

                Drone drone = cw2Client.getDroneDetails(dronePath.droneId());
                // TODO: adjust when dynamic ILP is implemented
                droneDetail = new DroneDetail(
                        drone.id(),
                        drone.capability().maxMoves(),
                        2,
                        DroneStatus.IDLE,
                        medDispatchRecs.get(0).date(),
                        medDispatchRecs.get(0).time()
                );
            } catch (Exception e) {
                logger.warn("Error while calculating delivery path", e);
                logger.info("Loading local example");
                DroneDeliveryInfo info = loadFallback();
                deliveryPath = info.deliveryPath();
                droneDetail = info.droneDetail();
            }
        }

        SimulatedDronePath simulatedDronePath =
                pathConversionService.convertPathToSimulatedDronePath(deliveryPath);
        SimulatedDroneInfo simulatedDroneInfo = new SimulatedDroneInfo(droneDetail,
                simulatedDronePath);

        droneLiveSimulator.startSimulation(simulatedDroneInfo);
    }

    private DroneDeliveryInfo loadFallback() {
        final PathLoader pathloader = new PathLoader();
        DeliveryPath deliveryPath = pathloader.loadPathJson();
        DroneDetail detail = pathloader.loadDroneDetailJson();
        return new DroneDeliveryInfo(detail, deliveryPath);
    }
}
