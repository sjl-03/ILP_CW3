package com.ilp.cw3.drone_simulator.service;

import com.ilp.cw3.drone_simulator.client.CW2Client;
import com.ilp.cw3.drone_simulator.model.*;
import com.ilp.cw3.drone_simulator.simulation.PathLoader;
import com.ilp.cw3.drone_simulator.simulation.SimulationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationService {
    private static final Logger logger =
            LoggerFactory.getLogger(SimulationService.class);

    private final CW2Client cw2Client;
    private final SimulationManager simulationManager;
    private final PathConversionService pathConversionService;

    public SimulationService(
            CW2Client cw2Client,
            SimulationManager simulationManager,
            PathConversionService pathConversionService){
        this.cw2Client = cw2Client;
        this.simulationManager = simulationManager;
        this.pathConversionService = pathConversionService;
    }

    // Assume one drone
    public void simulateDeliveryPath (
            List<MedDispatchRec> medDispatchRecs) {

        if (medDispatchRecs == null ||  medDispatchRecs.isEmpty()) {
            fallbackSimulation();
        }
        else{
            try {
                DeliveryPath deliveryPath =
                        cw2Client.calcDeliveryPath(medDispatchRecs);
                logger.info("deliveryPath: {}", deliveryPath);

                for (DronePath dronePath : deliveryPath.dronePaths()){

                    Drone drone = cw2Client.getDroneDetails(dronePath.droneId());
                    logger.info("drone: {}", drone);

                    DroneDetail droneDetail = new DroneDetail(
                            drone.id(),
                            drone.capability().maxMoves(),
                            2,
                            DroneStatus.IDLE,
                            // Choose an arbitrary date and time
                            medDispatchRecs.get(0).date(),
                            medDispatchRecs.get(0).time()
                    );

                    SimulatedDronePath simulatedDronePath =
                            pathConversionService.
                                    convertPathToSimulatedDronePath(
                                            dronePath);
                    SimulatedDroneInfo simulatedDroneInfo =
                            new SimulatedDroneInfo(
                                    droneDetail, simulatedDronePath);

                    simulationManager.enqueueSimulation(simulatedDroneInfo);
                }
            } catch (Exception e) {
                logger.warn("Error while calculating delivery path", e);
                logger.info("Loading local example");
                fallbackSimulation();
            }
        }

    }

    private void fallbackSimulation(){
        DroneDeliveryInfo info = loadFallback();
        DeliveryPath deliveryPath = info.deliveryPath();
        DroneDetail droneDetail = info.droneDetail();
        SimulatedDronePath simulatedDronePath =
                pathConversionService.
                        convertPathToSimulatedDronePath(
                                deliveryPath.dronePaths().get(0));
        SimulatedDroneInfo simulatedDroneInfo =
                new SimulatedDroneInfo(droneDetail, simulatedDronePath);
        simulationManager.enqueueSimulation(simulatedDroneInfo);
    }

    private DroneDeliveryInfo loadFallback() {
        final PathLoader pathloader = new PathLoader();
        DeliveryPath deliveryPath = pathloader.loadPathJson();
        DroneDetail detail = pathloader.loadDroneDetailJson();
        return new DroneDeliveryInfo(detail, deliveryPath);
    }
}
